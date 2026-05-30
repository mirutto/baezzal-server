# Verification Rules

- Any task that changes code or Gradle/build configuration is not complete until `rtk ./gradlew lintKotlin --continue` has been run successfully.
- If that verification fails, the work is not considered finished. Fix the issues that were introduced or uncovered and rerun the verification before closing the task.
