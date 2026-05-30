---
name: commit-style
description: Write Korean commit messages from code changes. Use when the user asks for commit messages, commit splitting, or commit grouping rules. Prefer the smallest meaningful unit of change, add a semantic prefix such as feat, docs, refactor, chore, and format each commit as a one-line title followed by short bullet points describing what is included.
metadata:
  short-description: 한국어 커밋 메시지 작성
---

# Commit Style

## Goal

Write commit messages based on the observed code changes. Always split changes into the smallest meaningful units, and format each commit as `prefix + title + short bullet list`.

## Output format

Always use the following format.

```markdown
feat: {한 줄 메시지}
- 작업 내용 1
- 작업 내용 2
```

If the work should be split into multiple commits, output consecutive commit blocks. Do not add extra commentary between them.

## Rules

1. The commit title and bullets must be written in Korean.
2. Keep the commit title to a single line.
3. Make the title short and specific.
4. Split by meaning, not by file.
5. Separate changes that can be reviewed independently.
6. If one commit contains multiple intents, split it.
7. Bullets should describe what was done, not why in long form.
8. Do not mention unfinished work or speculation.
9. Choose the prefix using the guide below.

## Prefix guide

- `feat`: 사용자 기능 추가 또는 기능 동작 확장
- `fix`: 버그 수정
- `refactor`: 동작 변화 없이 구조 개선
- `docs`: 문서 추가 또는 수정
- `test`: 테스트 추가 또는 수정
- `chore`: 설정, 정리, 잡무성 변경
- `build`: 빌드 설정, 의존성, 패키징 변경
- `ci`: CI/CD 설정 변경
- `perf`: 성능 개선
- `style`: 동작 영향 없는 포맷팅, 네이밍, 스타일 수정
- `revert`: 이전 커밋 되돌리기

## Splitting guidance

Use the following checks when deciding whether to split commits.

1. Split feature work from refactoring when both appear together.
2. Split documentation into a separate `docs` commit when it is more than a small companion change.
3. Distinguish between tests that validate a feature and tests that restructure test code.
4. Check whether configuration changes can stand alone from product code.
5. If the commit summary naturally wants many "and" clauses, it is probably too large and should be split again.

## Workflow

1. Group changes by intent first.
2. Decide whether each intent can stand as an independent commit.
3. Start from the smallest meaningful unit.
4. Assign the most accurate prefix to each commit block.
5. Write the title around the outcome, and bullets around included work.

## Preferred tone

- Prefer titles that show the action or outcome, not vague noun piles.
- Use concrete work names instead of exaggerated wording.
- Include domain terms and the actual target of change when helpful.

Good examples:

```markdown
feat: 사용자 프로필 이미지 변경 기능 추가
- 프로필 이미지 업로드 API를 연결
- 마이페이지에서 이미지 미리보기를 표시
```

```markdown
refactor: 주문 상태 계산 로직을 서비스로 분리
- 컨트롤러에 있던 상태 계산 코드를 서비스로 이동
- 상태 판별 조건을 메서드로 정리
```

Bad example:

```markdown
feat: 이것저것 수정
- 여러 부분 수정
```

## When the request is ambiguous

- If the changes are mixed, propose multiple commit blocks first.
- Unless there is a strong reason to keep them together, default to smaller commits.
