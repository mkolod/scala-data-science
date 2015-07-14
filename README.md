**Lambda Jam 2015 Talk Repo**
=====================================

**What's this?**
-------------------

This repo was prepared specifically for [my Lambda Jam talk](https://a.confui.com/-2qxYB1n4). This is an example of using Scala "design patterns" such as as type classes to abstract away the machine learning execution platform (e.g. Scala collections, Apache Spark RDDs) from the algorithm itself. This will allow you to easily add in another execution engine (e.g. Apache Flink) without having to rewrite any of the machine learning algorithms with specific bindings.

 Also, I show how easy it is to simplify the optimization code by using simple functional programming features such as being able to pass functions as arguments to other functions or methods. For instance, the optimize() method may need to accept a weight updating scheme (SGD, Adagrad), which is a function. Similarly, weight initialization, cost and gradient calculation can be functions that are passed in. Other Scala features such as monads are a given in this design.
 
**What do the packages contain?**
-----------------------------------------

The _optimization_ package contains optimization algorithms (SGD, Adagrad), optimization-specific data types (Datum, VectorizedData, etc.), sampling code, etc.

The _ml_ package contains cost and gradient functions that can be provided to the optimizer in order to solve a particular machine learning problem. For now, it's just linear regression, but if you look at the tiny amount of code found in CostFunctions and Gradients, you'll realize how easy it would be to add logistic regression or any other model.

The _plotting_ package contains utilities to abstract away some of the [WISP](https://github.com/quantifind/wisp) functionality, e.g. for doing multiple line plots on a single chart.

The _demo_ package contains an example with synthetically generated data for linear regression. We know what weights to expect from the model, and we train the model to check if it returns values close to the "ground truth" weights. The demo runs one example using stochastic gradient descent (SGD) against Scala collections, and another example using Adagrad against a Spark RDD. The results are plotted at the end.

**Feedback is appreciated!**
---------------------------------

If you have ideas for improving the design, adding new features, etc., please comment or issue a pull request. Thanks!
