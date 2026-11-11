# Health sweep

Snapshot of `main` at `cfaf1bd`, 2026-11-11.

## Workflows

All workflows are green on their last run. Three run only by hand or on a tag, so their last run
is not on `main`.

| Workflow | Last run | Result |
|---|---|---|
| `android.yml` | [42961477404](https://github.com/sanamhub/nepdate-mobile/actions/runs/42961477404) on `9a46e13` | Success. One commit behind `cfaf1bd`, which touched only docs and iOS and is outside this workflow's paths |
| `ios.yml` | [43027874744](https://github.com/sanamhub/nepdate-mobile/actions/runs/43027874744) on `cfaf1bd` | Success |
| `codeql.yml` | [43027874998](https://github.com/sanamhub/nepdate-mobile/actions/runs/43027874998) on `cfaf1bd` | Success |
| `scorecard.yml` | [43027874819](https://github.com/sanamhub/nepdate-mobile/actions/runs/43027874819) on `cfaf1bd` | Success, overall 6.8/10 |
| `android-release.yml` | [42649649697](https://github.com/sanamhub/nepdate-mobile/actions/runs/42649649697) on the beta branch | Success, dry run only; signing, attestation, Play and the draft release were skipped by design |
| `android-benchmark.yml` | [42766286072](https://github.com/sanamhub/nepdate-mobile/actions/runs/42766286072) on a branch | Success |
| `android-baseline-profile.yml` | [42766286105](https://github.com/sanamhub/nepdate-mobile/actions/runs/42766286105) on a branch | Success |

One iOS run was cancelled at 04:27, not failed: `cancel-in-progress` replaced it with the run for
the next commit. There is no red run on `main`.

## Alerts and findings

| Source | Open | Action |
|---|---|---|
| Dependabot alerts | 0 | None |
| Dependabot pull requests | 0 | None; the weekly schedule has a 14-day cooldown by design |
| CodeQL | 0 | None |
| Scorecard: branch protection | 1 | `main` has no ruleset requiring a pull request and passing checks. Changing it is a repository setting, which this work does not do. The owner should add the ruleset |
| Scorecard: code review | 1 | 0 of 23 changesets had an approving review; the repository has one maintainer. A process fix, not a code fix |
| Scorecard: pinned dependencies | 1 | Fixed here: the PyYAML install in `android.yml` now uses `--require-hashes` with the SHA-256 of the 6.0.3 wheel |
| Scorecard: binary artifacts | 1 | `gradle-wrapper.jar` is the Gradle wrapper, required by the build and checked in by Gradle. Explained, not removed |
| Scorecard: maintained | 1 | False positive: the repository is less than 90 days old |
| Scorecard: fuzzing, CII best practices | 2 | Aspirational checks, not defects |

## Branches

The branches for this work are the six pull requests for this batch. There are no stale branches
from this work. Nine older remote branches with merged pull requests remain; they are listed in
the batch report and were not deleted, because deleting them is repository history rather than a
code change.
