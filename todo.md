[x] optimize, especially process definition listing
[ ] Better error messaging: Error code, error title, error description, how to
    fix the error, url for more information.
[ ] Have a seperate option to run it directly from the command line without
    menus. help flags : --help, help, -h, also after subcommands for help for
    those
[ ] Ensure that you can get the version with camuanda-cli-tool --version, -V,
    version
[ ] It should also have a --verbose -v version
[ ] Introduce help option
[ ] Sometimes does not display process instance
[ ] Introduce command line for directly invoking more complicating commands
    like batch delete
[ ] use a composition of commands
[ ] Make it recognize keys without hitting enter
[ ] Rename next to something else like child

# Notes

* prefer flags to args:
    heroku fork FROMAPP --app TOAPP

    heroku fork --from FROMAPP --to TOAPP
* use -- argument to denote that it should stop parsing and pass everything
  down as an argument
  heroku run -a myapp -- myscript.sh -a arg1 means:
    foo=myscript.sh -a arg1
    heroku run -a myapp foo
* Use stderr for anything error related/general messaging
