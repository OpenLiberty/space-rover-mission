# Contributing to Open Liberty guides

Anyone can contribute to the Open Liberty guides and we welcome your contributions!

There are multiple ways to contribute: report bugs, fix bugs, contribute code, improve upon documentation, etc.

## Raising issues

Please raise any bug reports in this [guide repository](../../issues). For new topics, large updates to existing guides, or general suggestions and ideas, report your issue in the [Open Liberty common guides repository](https://github.com/OpenLiberty/guides-common/issues). Be sure to search the list of open issues to see if your issue has already been raised.

A good bug report makes it easy for everyone to understand what you were trying to do and what went wrong. Provide as much context as possible so we can try to recreate the issue.

## Contributions

Contributing to an Open Liberty guide is simple. Each guide is maintained in its own GitHub repository. To contribute, just fork from the `prod` branch for your chosen guide.  Then create a branch in your forked repository to include your contribution and open a pull request to the `staging` branch for the guide.
Certify the originality of your work by following the [Developer Certificate of Origin (DCO)](https://developercertificate.org) approach and adding a line to the end of the Git commit message to sign your work:

```text
Signed-off-by: Jane Williams <jane.williams@gmail.com>
```

The sign-off is just a line at the end of the commit message that certifies that you wrote it or otherwise have the right to pass it on as an open source patch.

Use your real name when you sign. We can't accept pseudonyms or anonymous contributions.

Many Git UI tools have support for adding the `Signed-off-by` line to the end of your commit message. This line can be automatically added by the `git commit` command by using the `-s` option.

The team is then notified and your contribution is reviewed according to the following process:

1. The team will review your change(s).
    - If there are further changes to be made, the team will request changes on the pull request.
    - If the team does not agree with the change, the PR will be closed with an explanation and suggestion for follow-up.
2. If the team approves, the team will merge your PR. A full Open Liberty staging site build will be run.
3. Based on the results of the build:
    - If further review is needed, we will let you know about a pending review from our team and discuss any necessary improvements that need to be made to your change(s).
    - If everything is successful, the team will publish your change(s) to the `prod` branch and update the Open Liberty production site, if necessary.

## Questions and concerns

If you have any questions or concerns about the guides or about Open Liberty, you can visit [Gitter for Open Liberty](https://gitter.im/OpenLiberty/) and post your questions in the relevant rooms. You can also join the Open Liberty group on [Groups.io](https://groups.io/g/openliberty) to discuss any issues you have.
