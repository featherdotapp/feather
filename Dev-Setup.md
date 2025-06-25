# Development Setup

## SonarQube for IDE

- Install the SonarQube for IDE plugin

## SonarCloud Integration with IntelliJ (SonarLint)

1. Open IntelliJ IDEA.
2. Go to Preferences (⌘ + ,) > Plugins.
3. Search for "SonarLint" and install the plugin.
4. Restart IntelliJ if prompted.
5. In IntelliJ, go to Preferences > Tools > SonarLint.
6. Click "Connect to SonarCloud".
7. Log in to your SonarCloud account (create one if needed).
8. Generate a SonarCloud token from your SonarCloud account (My Account > Security > Generate Tokens).
9. Enter the token in IntelliJ to authenticate.
10. Bind your project to the corresponding SonarCloud project.
11. SonarLint will now analyze your code using SonarCloud rules.
