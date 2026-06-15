# Verification Rules

- Any task that changes code or Gradle/build configuration is not complete until `rtk ./gradlew lintKotlin --continue` has been run successfully.
- Any task that changes code or Gradle/build configuration is not complete until `rtk ./gradlew test` has been run successfully.
- Any task that changes a JPA entity is not complete until the corresponding Flyway migration has been added or updated to keep the database schema in sync.
- Any task that changes a JPA entity in `community`, `recommendation`, `notification`, `media` must also verify that mirrored read models in `feed` and mirrored recommendation-side models still match the source table shape.
- Any task that changes a read-model JPA entity in `feed` must verify that its source table schema is still aligned with that read model.
- Any Flyway migration that removes table columns must also include the required cleanup `DELETE` statements for rows that would violate the new shape or constraints before the destructive schema change runs.
- If that verification fails, the work is not considered finished. Fix the issues that were introduced or uncovered and rerun the verification before closing the task.
