# docs/legal

| Item | Status | Record |
|---|---|---|
| Permission to port NepDate and use NepDateWidget as reference | granted (Raju Prasai, agreed with Sanam Pakuwal) | see `nepdate-rust/docs/legal/README.md` |
| Licence | MIT | `LICENSE` |
| Calendar data | MIT, from C# NepDate via the hub | hub `data/NOTICE` |
| Does a free digital calendar app need approval from the Nepal Panchanga Nirnayak Samiti? | **open**: ask before the first public release (M0-07); release gate in A3-04 and I3-03 | none yet |
| Copyright of compiled panchang data | data comes only from MIT NepDate; this app never scrapes | `docs/competitors.md` |
| Provenance of NepDate's tithi, holiday and event data | **open**: upstream's solution references a `HamroPatroScraper` tool that isn't published. Ask Raju where the data came from before the first public release; blocker for shipping the event data if it was scraped (hub `docs/upstream/proposals.md`, U-04). Release gate in A3-04 and I3-03 (ADR-0009); asked in M0-07 | none yet |
