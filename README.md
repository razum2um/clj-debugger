# Debugger

The missing tool in the ecosystem

## Usage

[![Clojars Project](http://clojars.org/debugger/latest-version.svg)](http://clojars.org/debugger)

```
user=> (debugger.core/foo 2)

Break from: /Users/razum2um/Code/debugger/src/debugger/core.clj:134 (type "(h)" for help)

   134: (defn foo [& args]
   135:   (let [x "world"
   136:         y '(1 2)
   137:         z (Object.)
=> 138:         ret (break (inc 42))]
   139:     (println "Exit foo with" ret)))

break user=> (h)

 (h)  prints this help
 (w)  prints code around breakpoint
 (l)  prints locals
 (c)  continues execution and preserves the result
 ^D   type Ctrl-D to exit break-repl and pass last result further

      you can also access locals directly and build sexp with them
      any last execution result (but `nil`) before exit will be passed further
      if last result is `nil` execution will continue normally

nil
break user=> (l)
{z #<Object java.lang.Object@6102a1f3>, y (1 2), x world, args (2)}
nil
break user=> (c)
43
break user=> (first y)
1
break user=> Exit foo with 1
nil
```

## Acknoledgements

- @richhickey for `clojure.main/repl`
- @GeorgeJahad for the lesson [how to preserve locals](https://github.com/GeorgeJahad/debug-repl/blob/master/src/alex_and_georges/debug_repl.clj#L68)

## License

Copyright © 2014 Vlad Bokov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
