# `repo-downloader`

Downloads certain commits from Github repositories given a YAML list.

## How to run?

From the root of `plugin-utilities` run
```shell
./gradlew downloadRepos -Prepos="path/to/repos.yaml" -Poutput="path/to/output/folder"
```

It will create a folder in `path/to/output/folder` for each cloned repository.

[Example of a list of repositories](repositoriesListExample.yaml)
