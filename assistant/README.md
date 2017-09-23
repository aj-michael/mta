A webserver that responds to a Google Assistant app to answer queries like
"When is the next Manhattan bound L train at Bedford Avenue?".


Prerequisites: JRuby 2.2 or later, JDK8 and `//java/net/ajmichael/mta:mta_deploy.jar`.

Run from the top level as:

```
CLASSPATH=bazel-bin/java/net/ajmichael/mta/mta_deploy.jar jruby assistant/server.rb
```
