#!/usr/bin/env python3
"""Static checks for .github/workflows/android-release.yml (A3-02).

The workflow cannot run until a human creates an android-v* tag and approves the production
environment. This test parses the YAML with PyYAML, checks the structure the plan requires, and
runs one negative fixture per rule. It is not a regex over the text.
"""

from __future__ import annotations

import sys
from pathlib import Path
from typing import Any, Callable

import yaml

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "android-release.yml"
FIXTURES = ROOT / "tools" / "workflow-fixtures"
PINNED = set("0123456789abcdef")


class WorkflowError(Exception):
    """A workflow rule failed."""


class GithubLoader(yaml.SafeLoader):
    """PyYAML follows YAML 1.1, where the bare key `on` becomes True."""


def _mapping(loader: GithubLoader, node: yaml.Node, deep: bool = False) -> dict[Any, Any]:
    loader.flatten_mapping(node)
    result: dict[Any, Any] = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key is True:
            key = "on"
        elif key is False:
            key = "off"
        result[key] = loader.construct_object(value_node, deep=deep)
    return result


GithubLoader.add_constructor("tag:yaml.org,2002:map", _mapping)


def load(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8").replace("\r\n", "\n")
    value = yaml.load(text, Loader=GithubLoader)
    if not isinstance(value, dict):
        raise WorkflowError(f"{path.name} is not a YAML mapping")
    return value


def need_map(value: Any, name: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise WorkflowError(f"{name} must be a mapping")
    return value


def need_list(value: Any, name: str) -> list[Any]:
    if not isinstance(value, list):
        raise WorkflowError(f"{name} must be a list")
    return value


def jobs(doc: dict[str, Any]) -> dict[str, Any]:
    return need_map(doc.get("jobs"), "jobs")


def steps(job: dict[str, Any], name: str = "job") -> list[dict[str, Any]]:
    raw = need_list(job.get("steps"), f"{name}.steps")
    return [need_map(step, f"{name}.steps[]") for step in raw]


def run_text(job: dict[str, Any]) -> str:
    return "\n".join(str(step.get("run", "")) for step in steps(job))


def check_triggers(doc: dict[str, Any]) -> None:
    on = need_map(doc.get("on"), "on")
    if set(on) != {"push", "workflow_dispatch"}:
        raise WorkflowError("triggers must be exactly push (tags) and workflow_dispatch")
    tags = need_list(need_map(on["push"], "on.push").get("tags"), "on.push.tags")
    if tags != ["android-v*"]:
        raise WorkflowError("push trigger must be exactly the android-v* tag")
    dispatch = need_map(on["workflow_dispatch"], "on.workflow_dispatch")
    dry_run = need_map(need_map(dispatch.get("inputs"), "on.workflow_dispatch.inputs").get("dry_run"), "dry_run")
    if dry_run.get("type") != "boolean":
        raise WorkflowError("dry_run input must be a boolean")
    if dry_run.get("default") is not True:
        raise WorkflowError("dry_run input must default to true")


def collect_uses(value: Any) -> list[str]:
    found: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            if key == "uses" and isinstance(child, str):
                found.append(child)
            else:
                found.extend(collect_uses(child))
    elif isinstance(value, list):
        for child in value:
            found.extend(collect_uses(child))
    return found


def check_action_pins(doc: dict[str, Any]) -> None:
    for action in collect_uses(doc):
        if "@" not in action:
            raise WorkflowError(f"action {action!r} is not pinned")
        name, reference = action.split("@", 1)
        if not name or name.startswith(".") or len(reference) != 40 or set(reference) - PINNED:
            raise WorkflowError(f"action {action!r} is not pinned to a 40-digit commit")


def check_permissions(doc: dict[str, Any]) -> None:
    permissions = need_map(doc.get("permissions"), "permissions")
    if permissions != {"contents": "read"}:
        raise WorkflowError("top-level permissions must be exactly contents: read")
    for name, job_value in jobs(doc).items():
        job = need_map(job_value, f"jobs.{name}")
        job_permissions = job.get("permissions")
        if job_permissions is None:
            continue
        permissions = need_map(job_permissions, f"jobs.{name}.permissions")
        if name == "attest":
            expected = {"contents": "read", "id-token": "write", "attestations": "write"}
            if permissions != expected:
                raise WorkflowError(
                    "attest job permissions must be contents read, id-token write and attestations write"
                )
        elif name == "release":
            if permissions != {"contents": "write"}:
                raise WorkflowError("release job permissions must be exactly contents: write")
        elif "id-token" in permissions or "attestations" in permissions:
            raise WorkflowError(f"only the attest job may have id-token or attestations ({name})")


def check_production_gates(doc: dict[str, Any]) -> None:
    all_jobs = jobs(doc)
    for name in ("preflight", "ci", "sign", "publish", "attest", "release"):
        if name not in all_jobs:
            raise WorkflowError(f"workflow has no {name} job")
    for name in ("sign", "publish", "release"):
        job = need_map(all_jobs[name], f"jobs.{name}")
        environment = job.get("environment")
        if not isinstance(environment, str) or environment != "production":
            raise WorkflowError(f"{name} must use environment production")
        condition = str(job.get("if", ""))
        if "github.ref_type == 'tag'" not in condition:
            raise WorkflowError(f"{name} must require a tag")
        if "inputs.dry_run == false" not in condition:
            raise WorkflowError(f"{name} must require dry_run false")


def check_preflight(doc: dict[str, Any]) -> None:
    preflight = need_map(jobs(doc)["preflight"], "jobs.preflight")
    text = "\n".join(str(step) for step in steps(preflight))
    for expected in (
        "versionName",
        "CHANGELOG.md",
        "DataFreshnessTest",
        "RELEASE_DATE",
        "release-data-status.txt",
    ):
        if expected not in text:
            raise WorkflowError(f"preflight does not use {expected}")


def check_signing(doc: dict[str, Any]) -> None:
    sign = need_map(jobs(doc)["sign"], "jobs.sign")
    text = run_text(sign)
    if "assembleRelease" not in text or "bundleRelease" not in text:
        raise WorkflowError("sign job does not build the release APK and AAB")
    step_text = "\n".join(str(step) for step in steps(sign))
    if "ANDROID_KEYSTORE_BASE64" not in step_text or "ANDROID_KEYSTORE_PATH" not in step_text:
        raise WorkflowError("sign job does not write the release keystore from the production secret")
    if "apksigner verify" not in text or "jarsigner -verify" not in text:
        raise WorkflowError("sign job does not verify both signatures")
    if "sha256sum" not in text:
        raise WorkflowError("sign job does not write SHA256SUMS")


def check_publish(doc: dict[str, Any]) -> None:
    publish = need_map(jobs(doc)["publish"], "jobs.publish")
    text = run_text(publish)
    for expected in ("publishReleaseBundle", "--artifact-dir", "--track=internal"):
        if expected not in text:
            raise WorkflowError(f"publish job does not use {expected}")
    if "ANDROID_PUBLISHER_CREDENTIALS" not in "\n".join(str(step) for step in steps(publish)):
        raise WorkflowError("publish job does not use the Play service-account secret")


def check_attestation(doc: dict[str, Any]) -> None:
    attest = need_map(jobs(doc)["attest"], "jobs.attest")
    text = "\n".join(str(step) for step in steps(attest))
    if "actions/attest@" not in text:
        raise WorkflowError("attest job does not run actions/attest")
    if "subject-path" not in text:
        raise WorkflowError("attest job does not name the artifacts")


def check_release(doc: dict[str, Any]) -> None:
    release = need_map(jobs(doc)["release"], "jobs.release")
    found = False
    for step in steps(release):
        action = step.get("uses")
        if isinstance(action, str) and action.startswith("softprops/action-gh-release@"):
            with_values = need_map(step.get("with"), "release.with")
            if with_values.get("draft") is not True:
                raise WorkflowError("release job must create a draft")
            files = str(with_values.get("files", ""))
            for expected in (".apk", "SHA256SUMS", "attestation.json"):
                if expected not in files:
                    raise WorkflowError(f"draft release does not attach {expected}")
            found = True
    if not found:
        raise WorkflowError("release job does not use softprops/action-gh-release")


def check_no_expression_in_run(doc: dict[str, Any]) -> None:
    for name, job_value in jobs(doc).items():
        if "${{" in run_text(need_map(job_value, f"jobs.{name}")):
            raise WorkflowError(f"{name} puts a ${{{{ }}}} expression inside run")


def check(doc: dict[str, Any]) -> None:
    if doc.get("name") != "Android release":
        raise WorkflowError("workflow name must be Android release")
    check_triggers(doc)
    check_action_pins(doc)
    check_permissions(doc)
    check_production_gates(doc)
    check_preflight(doc)
    check_signing(doc)
    check_publish(doc)
    check_attestation(doc)
    check_release(doc)
    check_no_expression_in_run(doc)


def expect_failure(path: Path, checker: Callable[[dict[str, Any]], None], needle: str) -> None:
    try:
        checker(load(path))
    except WorkflowError as error:
        if needle not in str(error):
            raise AssertionError(f"{path.name}: unexpected error: {error}") from error
        print(f"negative control OK: {path.name}: {error}")
        return
    raise AssertionError(f"{path.name}: fixture passed but must fail")


def main() -> int:
    workflow = load(WORKFLOW)
    check(workflow)
    print(f"positive check OK: {WORKFLOW.relative_to(ROOT)}")

    expect_failure(
        FIXTURES / "trigger-pull-request.yml",
        check_triggers,
        "triggers must be exactly",
    )
    expect_failure(
        FIXTURES / "unpinned-action.yml",
        check_action_pins,
        "not pinned",
    )
    expect_failure(
        FIXTURES / "id-token-outside-attest.yml",
        check_permissions,
        "only the attest job",
    )
    expect_failure(
        FIXTURES / "signing-without-production.yml",
        check_production_gates,
        "must use environment production",
    )
    expect_failure(
        FIXTURES / "dry-run-false.yml",
        check_triggers,
        "default to true",
    )
    print("all static workflow checks passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
