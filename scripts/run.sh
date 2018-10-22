#!/bin/zsh

scriptdir=$(dirname "$0")

version=$(grep defproject $scriptdir/../project.clj | cut -d' ' -f3)
size=${#version}
trimmed_version=${version:1:$size-2}
java -jar $scriptdir/../target/camunda-cli-tool-$trimmed_version-standalone.jar "$@"
