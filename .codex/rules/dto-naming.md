# DTO Naming Rules

- Use `*Command` for request DTOs that enter the application layer.
- Use `*Result` for the final response DTO of a single use case.
- Use `*Data` for reusable response fragments that describe a single entity and may be nested inside another response or wrapped in a list.

## Core Intent

- `Command` expresses input to a use case.
- `Result` expresses the completed output of a use case.
- `Data` expresses reusable entity-shaped response data.

## Examples

- `MemberOnboardingCommand`
- `MemberOnboardingResult`
- `MemberData`

## Additional Naming

- The default is `Command`, `Result`, and `Data`.
- Allow a different suffix only when it communicates a more specific role more clearly than the default naming.
- Typical allowed examples are `*Query`, `*Event`, `*Payload`, `*Summary`, `*SliceResult`, and `*PageResult`.

## Detailed Guidance

- Prefer domain-focused names for `*Data`.
  - Prefer `MemberData` over `MemberOnboardingData`.
- Prefer use-case-focused names for `*Result`.
  - Prefer `MemberOnboardingResult` or `GetMemberResult`.
- Prefer action-focused names for `*Command`.
  - Prefer `CreateMemberCommand` or `UpdateMemberProfileCommand`.
