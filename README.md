# Debugger

[![Build Status](https://travis-ci.org/razum2um/clj-debugger.svg?branch=master)](https://travis-ci.org/razum2um/clj-debugger)

The missing tool in the Clojure ecosystem.

## Usage

[![Clojars Project](http://clojars.org/debugger/latest-version.svg)](http://clojars.org/debugger)

```clj
(use 'debugger.core)

(break (some-fn))

(break-catch (some-raising-fn))
```

## Breakpoints

```
user=> (dotimes [n 5] (debugger.core-test/foo n))

Break from: /Users/razum2um/Code/debugger/src/debugger/core_test.clj:12 (type "(help)" for help)

   17:         e (fn [] nil)
   18:         x "world"
   19:         y '(8 9)
   20:         z (Object.)
=> 21:         ret (break (inc 42))]
   22:     (println "Exit foo with" ret)))
```

## Interactive help

```
debugger.core-test/qux:31=> (h)

 (h)    (help)          prints this help
        (wtf)           prints short code of breakpointed function
        (wtf??)         prints full code of breakpointed function
 (l)    (locals)        prints locals
 (c)    (continue)      continues execution, preserves the result and will break here again
        (skip 3)        skips next 3 breakpoints in this place
 (q)    (quit)          or type Ctrl-D to exit break-repl, pass last result further, will never break here anymore

                        use (debugger.core/reset-skips!) if breaks are skipped
                        you can also access locals directly and build sexp with them
                        any last execution result (but `nil`) before exit will be passed further
                        if last result is `nil` execution will continue normally

nil
```

## Locals access

```
debugger.core-test/foo:21=> (l)
{x "world",
 a [1 2],
 y (8 9),
 args nil,
 e #<core_test$foo$e__4735 debugger.core_test$foo$e__4735@6735cbba>,
 z #<Object java.lang.Object@2094643d>,
 h {:k "v"},
 b #{4 3},
 d nil}
nil
debugger.core-test/foo:21=> z
#<Object java.lang.Object@3dc76ae9>
```

## Return value control

Use any non-nil value to fake inner result:

```
user=> (debugger.core-test/foo)

Break from: /Users/razum2um/Code/debugger/src/debugger/core_test.clj:12 (type "(help)" for help)

   17:         e (fn [] nil)
   18:         x "world"
   19:         y '(8 9)
   20:         z (Object.)
=> 21:         ret (break (inc 42))]
   22:     (println "Exit foo with" ret)))

debugger.core-test/foo:21=> 1                     ;; inner (inc 42) won't be called
1
debugger.core-test/foo:21=> Exit foo with 1       ;; used result from REPL
nil
```

## Code expection

```
debugger.core-test/foo:21=> (whereami)

   12: (defn foo [& args]
   13:   (let [a [1 2]
   14:         b #{3 4}
   15:         h {:k "v"}
   16:         d nil
   17:         e (fn [] nil)
   18:         x "world"
   19:         y '(8 9)
   20:         z (Object.)
=> 21:         ret (break (inc 42))]
   22:     (println "Exit foo with" ret)))

nil
```

## Stack expection

```
debugger.core-test/foo:21=> (wtf??)

  [0]                      core_test.clj:21     debugger.core-test/foo
  [1]                        RestFn.java:408    clojure.lang.RestFn
  [2]   form-init1204084863726891616.clj:1      user/eval3682
  [3]                      Compiler.java:6703   clojure.lang.Compiler
  [4]                      Compiler.java:6666   clojure.lang.Compiler
  [5]                           core.clj:2927   clojure.core/eval
  [6]                           main.clj:239    clojure.main/repl
  [7]                           main.clj:239    clojure.main/repl
  [8]                           main.clj:257    clojure.main/repl
  [9]                           main.clj:257    clojure.main/repl
 [10]                        RestFn.java:1523   clojure.lang.RestFn
 [11]             interruptible_eval.clj:67     clojure.tools.nrepl.middleware.interruptible-eval/evaluate
 [12]                           AFn.java:152    clojure.lang.AFn
 [13]                           AFn.java:144    clojure.lang.AFn
 [14]                           core.clj:624    clojure.core/apply
 [15]                           core.clj:1862   clojure.core/with-bindings*
 [16]                        RestFn.java:425    clojure.lang.RestFn
 [17]             interruptible_eval.clj:51     clojure.tools.nrepl.middleware.interruptible-eval/evaluate
 [18]             interruptible_eval.clj:183    clojure.tools.nrepl.middleware.interruptible-eval/interruptible-eval
 [19]             interruptible_eval.clj:152    clojure.tools.nrepl.middleware.interruptible-eval/run-next
 [20]                           AFn.java:22     clojure.lang.AFn
 [21]            ThreadPoolExecutor.java:1142   java.util.concurrent.ThreadPoolExecutor
 [22]            ThreadPoolExecutor.java:617    java.util.concurrent.ThreadPoolExecutor/Worker
 [23]                        Thread.java:745    java.lang.Thread

nil

```

## Break on next `(break)` or skip it

```
debugger.core-test/foo:21=> (c)
43
Exit foo with #<Object java.lang.Object@3dc76ae9>

Break from: /Users/razum2um/Code/debugger/src/debugger/core_test.clj:12 (type "(help)" for help)

   17:         e (fn [] nil)
   18:         x "world"
   19:         y '(8 9)
   20:         z (Object.)
=> 21:         ret (break (inc 42))]
   22:     (println "Exit foo with" ret)))

debugger.core-test/foo:21=> (skip 3)
nil
Exit foo with 43
Exit foo with 43
Exit foo with 43

Break from: /Users/razum2um/Code/debugger/src/debugger/core_test.clj:12 (type "(help)" for help)

   17:         e (fn [] nil)
   18:         x "world"
   19:         y '(8 9)
   20:         z (Object.)
=> 21:         ret (break (inc 42))]
   22:     (println "Exit foo with" ret)))

debugger.core-test/foo:21=> (c)
43
Exit foo with 43
nil
```

## Breakpoints in threads
```
user> (debugger.core-test/in-thread)
nil
Registered breakpoint: debugger.core_test$foo$breakpoint__20396__auto____20431@4c18a68e. 
Type (breakpoints) to see a list of registered breakpoint, and (connect) to connect to one.
user> (breakpoints)
Breakpoints:
0) debugger.core_test$foo$breakpoint__20396__auto____20431@4c18a68e
nil
user> (connect)

Break from: /Users/maxim/tmp/clj-debugger/src/debugger/core_test.clj:8 (type "(help)" for help)

   13:         e (fn [] nil)
   14:         x "world"
   15:         y '(8 9)
   16:         z (Object.)
=> 17:         ret (break (inc 42))]
   18:     (println "Exit foo with" ret))) 

debugger.core-test/foo:17=> a
[1 2]
debugger.core-test/foo:17=> (q)
Quitting debugger...
nil
nilExit foo with 
[1 2]
user> 
```

## Configuration

The following dynamic vars are configurable in `debugger.config`

| Var | Default Value | Use |
| ---| --- | --- |
| `*break-outside-repl*` | `true` | Break in non-repl threads, since 0.2.0. Default value was  `false` in 0.1.x. |
| `*code-context-lines*` | 5 | Number of lines to include when showing source. |
| `*locals-print-length*` | 10 | Print-length for locals inside the debugger.   |
| `*skip-repl-if-last-quit-ago*` |2 | Number of seconds to wait after a debugger is quit before starting another one. |

## TODO

- `(step)`, `(up)`, `(down)` stack manipulation

## Acknowledgements

- [@richhickey](http://github.com/richhichey) for `clojure.main/repl`
- [@GeorgeJahad](http://github.com/GeorgeJahad) for the lesson [how to preserve locals](https://github.com/GeorgeJahad/debug-repl/blob/master/src/alex_and_georges/debug_repl.clj#L68)
- [@mveytsman](https://github.com/mveytsman) for breakpoints in threads

## YourKit

<img src="http://www.yourkit.com/images/yklogo.png"></img>

YourKit has given an open source license for their profiler, greatly simplifying the profiling of ClojureScript performance.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a>
and <a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

## License

Copyright © 2014-2015 Vlad Bokov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
