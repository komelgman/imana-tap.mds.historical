This repository was generated from the Imanai TAP service template.
Before starting development, complete the repository setup checklist below.

**Repository Setup Checklist**

1. **[Configure Branch Protection](https://github.com/komelgman/${newRepo}/settings/branches)**
   Configure branch protection rules in the repository settings (e.g., enforce pull-request reviews, require status checks before merging).

2. **Disable Unused Repository Features**
   In the repository settings, disable features that are not used by this project (for example: Wiki, Projects, Discussions).

3. **Add the Project to SonarCloud**
   Create a new project in SonarCloud and link it to this repository.

4. **[Add Required GitHub Actions Secrets](https://github.com/komelgman/${newRepo}/settings/secrets/actions)**
   Add the following secrets in *Settings → Secrets and variables → Actions*:

    * `GH_PACKAGES_READ_TOKEN` — required for accessing GitHub Packages (e.g., parent POM, dependencies).
    * `SONAR_TOKEN` — required for SonarCloud analysis in GitHub Actions.
   
   Make sure the `SONAR_TOKEN` secret matches the token generated in SonarCloud for CI analysis.

After completing the checklist:

* Remove these setup instructions from the README.
* Fill in the service description template below.
* Create a Pull Request to ensure CI, branch protection, secrets, and SonarCloud integration work correctly.

**Service README Template (keep this section)**

---

# Service Name

## Overview

Short description of the service: purpose, responsibilities, and its role in the Imanai TAP platform.

## Configuration

TBD. Environment variables, required secrets, and runtime configuration.

## Local Development

TBD. How to run the service locally, prerequisites, commands, and useful scripts.

## CI/CD

TBD. Short notes on how CI/CD works for this service (build, test, SonarCloud, deployment pipeline).
