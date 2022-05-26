Notes for Katie:

This project was my attempt to take some of my boss' code that I use at
work and attempt to revamp the code into a more functional style. There
are some limitations I discovered while doing this:

1. This code uses `jakarta.servlet` so all code has to run through those class overloads
2. Since this is a web server, a large chunk of the code is fundamentally IO. So finding ways to functionalize are limited to where there is less IO directly happening.

All that said my major accomplishments this term:

1. Replace some functions with regex pattern matching.
2. Update the code from using Tomcat 8 to Tomcat 10 (ie javax.servlet to jakarta.servlet)
3. Functionalize `LayoutFilter` by pulling out immutable data and breaking up larger functions into operations on the record 36007559c04e1a037430346a556c16e1cda5acb8
4. Rework a couple of complicated loops into tail recursive functions, 03c72920d5fc619e2d9eabaf7c6b9e213b21db63 (by doing this I actually discovered a bug in the original code!)
5. Rework most of the other code to remove the majority of mutable variables, mutable data structures, and switch out manual loops with map, filter, etc.

More relevant to the advanced part of the class I also discovered a "monoid" pattern in some of the code
where context paths would be concatenated in a special way so that `"/path1/" + "/path2/" == "/path1/path2/"`.
So I created a `Path` monoid that wrapped this pattern. f1f70589d1d70e94cb07910a70a956192311f5ad

Another nice side effect of this is that I reduced the total number of lines of code by ~300 lines. So the code is
more correct and more succinct.