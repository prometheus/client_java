# Docs

This directory contains [hugo](https://gohugo.io) documentation to be published in GitHub pages.

## Run Locally

```shell
hugo server -D
```

This will serve the docs on [http://localhost:1313](http://localhost:1313).

## Deploy to GitHub Pages

Changes to the `main` branch will be deployed automatically with GitHub Actions.

## Update Javadoc

Javadoc are not checked-in to the GitHub repository.
They are generated on the fly by GitHub Actions when the docs are updated.
To view locally, run the following:

```shell
# note that the 'compile' in the following command is necessary for
# Javadoc to detect the module structure
./mvnw clean compile javadoc:javadoc javadoc:aggregate
rm -r ./docs/static/api
mv ./target/site/apidocs ./docs/static/api
```

GitHub pages are in the `/client_java/` folder, so we link to `/client_java/api` rather than `/api`.
To make JavaDoc work locally, create a link:

```shell
mkdir ./docs/static/client_java
ln -s ../api ./docs/static/client_java/api
```

## Update Geekdocs

The docs use the [Geekdocs](https://geekdocs.de/) theme. The theme is checked in to GitHub in the
`./docs/themes/hugo-geekdoc/` folder. To update [Geekdocs](https://geekdocs.de/), remove the current
folder and create a new one with the
latest [release](https://github.com/thegeeklab/hugo-geekdoc/releases). There are no local
modifications in `./docs/themes/hugo-geekdoc/`.

## Notes

Here's how the initial `docs/` folder was set up:

```shell
hugo new site docs
cd docs/
mkdir -p themes/hugo-geekdoc/
curl -L https://github.com/thegeeklab/hugo-geekdoc/releases/download/v0.41.1/hugo-geekdoc.tar.gz \
  | tar -xz -C themes/hugo-geekdoc/ --strip-components=1
```

Create the initial `hugo.toml` file as described
in [https://geekdocs.de/usage/getting-started/](https://geekdocs.de/usage/getting-started/).
