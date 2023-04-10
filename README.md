# cpg-test

A Clojure tool designed to help to debloat java (<11) projects.

## Usage

The following flags and default values are valid
-r, --project-root ROOT     .   Project root
-j, --jar-paths PATHS       []  unmanaged .jar directories, separated by ";"
-s, --external-sinks SINKS  []  external sink definitions as .json files, separated by ";"
-h, --help

### --project-root
The root directory of the project to analyze, the default value is the current directory

### --jar-paths
Paths to directories containing additional jar files which may be loaded by the analysed project

### --external-sinks
Definition of Methods which can load classes in the JVM. This tool can only analyse source code written in java. So if
you want to analyse your project without the dependent source code, you have to manually which methods can load classes
and how to construct the name of the loaded class from the parameters.
The format used to specify external sinks is explained [here](#definition-of-external-sinks)

## Definition of external sinks
External sinks are defined in the JSON format. There are two possibilities to define sinks.

A "simple" sink is a sink which equates the fully qualified class name by prepending a prefix and appending a suffix to
argument passed. A simple sink is a function with an arity of one.

A "pattern" sink is a sink which equates the fully qualified class name by combining multiple arguments. A pattern sink
can have any arity. A java format pattern specifies how the arguments are combined to the class name.

```
{
  "simple": [
    {
      "name": "org.example.App.external",
      "prefix": "com.test",
      "suffix": "lolz"
    },
    {
      "name": "com.my.hidden.Library.load",
      "prefix": "com.my.loaded.",
      "suffix": ".Stuff"
    }
  ],
  "pattern": [
    {
      "name": "org.example.App.external2",
      "pattern": "%1$s.%1$s.%4$s%2$s"
    }
  ]
}
```

## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
