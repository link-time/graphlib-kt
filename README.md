# graphlib-kt
[![Actions Status](https://github.com/link-time/graphlib-kt/workflows/Kotlin%20CI/badge.svg)](https://github.com/link-time/graphlib-kt/actions)
[![GitHub License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Kotlin library for immutable graphs

# Building 
Gradle is used to manage the build so `./gradlew` should suffice.

# Overview
## Currently supported graphs
### DirectedGraph
A directed graph (or digraph) is a graph that is made up of a set of vertices connected by edges, where the edges have a direction associated with them. 

![Image of Digraph](doc/images/digraph.png)
#### Construction
A new instance can be constructed from a list of Edges

## Introduced Types 
  * Edge - data class to represent edges within a graph
  * Tree - for tree representations of a graph
