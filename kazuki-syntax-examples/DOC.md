
Rules:
1. Recursive functions must be lazy with defined type and should

Style/perf guidelines:
1. Toolkit/utility functions defined on objects or files should be lazy -- thus they will only be instantiated if used
2. Types should only be added to function definition or command; they should not be declared explicitly in pre, post, measure closures

Tips
1. You cannot create an inner 'functions' class if you 