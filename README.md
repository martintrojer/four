# 4k Clojure Demo Competition


Me and [Martin](martinsprogrammingblog.blogspot.co.uk) were discussing the feasibility of this and quickly ended up in a debate about the rules, which he won.
More or less, your demo has to be a 4k stripped Clojure jar, that can be run like this:

    java -cp clojure-1.4.0.jar:target/four.jar four

    $ ls -l target/four.jar
    -rw-rw-r-- 1 hraberg hraberg 2059 2012-08-22 10:55 target/four.jar

    $ unzip -l target/four.jar
    Archive:  target/four.jar
    Length      Date    Time    Name
    ---------  ---------- -----   ----
        100  2012-08-22 10:55   META-INF/MANIFEST.MF
       1571  2012-08-22 10:16   four.clj
       1732  2012-08-22 10:55   four.class
    ---------                     -------
       3403                     3 files


### Rules

Fork and go. The bash script [`build`](https://github.com/hraberg/four/blob/master/build) will produce (and run) a jar that satisfies the limitations.
Dependencies and resources are allowed, but obviously need to fit the size limit. As can be seen above, PACK200 is allowed, as is [ProGuard](http://proguard.sourceforge.net/), which the `build` script uses. Pulling resources from the network or local drive is not.


### Example

[`four.clj`](https://github.com/hraberg/four/blob/master/src/four.clj) is an experiment that sets up a few seqs / fns for rendering over a timeline in Graphics2D.
The animation itself is just a flickering of colors, but will obviously soon evolve into a magnificent real time [path tracer](http://www.kevinbeason.com/smallpt/).


## References

[Java4k Game Rules](http://www.java4k.com/index.php?action=view&page=rulesjudg)

[scene.org Best 4k Intro (2011)](http://awards.scene.org/awards.php?year=2011&cat=10)

[Assembly 4k Archive](http://archive.assembly.org/2011/4k-intro)

[Notch's Left 4k Dead](http://www.mojang.com/notch/j4k/l4kd/)
