# Project coding workflow

## Lead
- The main agent owns requirements, planning, integration, and verification.
- Briefly plan multi-step changes before editing.
- Handle small or tightly coupled changes directly.

## Implementation
- Delegate suitable bounded implementation tasks to subagents.
- Explicitly request model gpt-5.6-luna with reasoning effort max.
- Give each worker a clear goal, allowed files, and acceptance criteria.
- Keep concurrent editing scopes disjoint.
- Workers must preserve unrelated changes and avoid expanding scope.
- Workers must not spawn further agents.
- Inspect and integrate their changes before accepting them.

## Review
- After substantial implementation, or when explicitly requested,
  spawn a reviewer using gpt-5.6-sol with reasoning effort med.
- The reviewer must inspect without editing files.
- Focus on correctness, regressions, requirement mismatches,
  and important test gaps.
- Address relevant findings and rerun affected checks.

## Simplicity and verification
- Implement only requested or necessary behavior.
- Follow existing project conventions.
- Avoid unnecessary dependencies, abstractions, and infrastructure.
- Run relevant checks and add only high-value tests.
- Report changed files, verification results, and remaining issues.

## Delegation limitations
- If spawning or explicit model selection is unavailable, report it.
- Do not silently substitute another model or simulate delegation.
- Distinguish requested model settings from verified runtime metadata.

## Agent activity reporting

- Before delegating, announce:
    - Role: implementation worker or reviewer
    - Requested model and reasoning effort
    - Assigned task

- When a subagent finishes, report:
    - Its role and result
    - Whether it completed successfully
    - Verified runtime model and effort, if metadata exposes them

- Distinguish requested settings from verified runtime settings.
- If runtime metadata is unavailable, say:
  "Requested: [model / effort]. Actual runtime model: unverified."
- Never use a subagent's self-description as proof of its model.
- Keep these updates brief.