# Packed Framework: Application Model

## Overview

Packed provides a flexible application model that supports both simple standalone applications and complex hierarchical systems. The framework manages applications through two complementary relationship types: compositional ownership and functional dependencies.

## Application Fundamentals

Most end-user applications consist of a single application. However, it is possible to create more elaborate applications by combining multiple applications into one. The primary purpose of splitting into multiple applications is **containment** - creating clear boundaries around functionality.

It's important to understand that splitting into multiple applications doesn't enable functionality that couldn't be achieved in a single application. Rather, it provides superior organization, isolation, and maintainability of code. For example, you might have:

- A root application serving as the main entry point
- Multiple specialized service applications (authentication, persistence, etc.)
- Dynamic worker applications that can scale up/down based on demand
- Embedded utility applications shared across other applications

This modular approach allows functionality to be clearly separated even within a single JVM process. The root application can be created initially, with child applications dynamically added at runtime through the framework. This runtime composition provides flexibility without requiring separate deployment or processes.

## Relationship Types

### Compositional Relationship (Ownership) Application Ownership Tree

Application ownership is organized in a tree with a single root application at the top. This root application can then have multiple child applications of different types.

Key characteristics:
- Each application has exactly one parent (except the root)
- Parent applications control the lifecycle of their children
- When a parent application closes, all its children are closed in an orderly fashion
- Resource allocation and cleanup follows the ownership hierarchy
- Child applications can be:
  - Singleton instances (e.g., a search service)
  - Multiple instances (e.g., worker agents)
  - Dynamically created/destroyed

### Functional Relationship (Application Dependency Network Graph)

Inside a hierarchical application (an application that does not consist of just one application), applications have a dependency network that determines how applications are related both in terms of dependency injection and lifecycle management.

Key characteristics:
- Dependencies can cross ownership boundaries
- Applications cannot start until their dependencies are ready
- Applications are shut down in reverse dependency order
- Service discovery operates through the dependency network
- Dependencies can be:
  - Required (application cannot function without them)
  - Optional (application can operate in degraded mode)
  - Versioned (specific compatibility requirements)

## Lifecycle Management

The dual model of ownership and dependencies directly influences application lifecycle:

1. **Initialization Phase**
   - Applications are constructed following the ownership tree
   - Dependencies are wired across the network
   - Initialization proceeds in topological dependency order

2. **Runtime Phase**
   - Service requests flow through the dependency network
   - Resource management follows the ownership hierarchy
   - Health and status information propagates in both directions

3. **Shutdown Phase**
   - Initiated from the ownership tree (top-down)
   - Executed in reverse dependency order
   - Ensures dependent applications are never left in an invalid state

## Practical Example

Consider a distributed processing system:
- **Root Application**: Manages overall system configuration and UI
- **Search Service**: Singleton embedded application for indexing/querying
- **Worker Agents**: Multiple instances that process data

In this scenario:
- The root application owns both the search service and worker agents (tree relationship)
- Worker agents depend on the search service for querying (graph relationship)
- When shutting down:
  1. Worker agents are stopped first (as they depend on the search service)
  2. Search service is stopped next
  3. Root application shuts down last

This model ensures that no application attempts to use a service that has already been shut down, while maintaining clear ownership boundaries for resource management.

## Benefits

- **Strong Containment**: Applications provide clear boundaries around functionality and resources
- **Modular Development**: Components can be developed, tested, and deployed independently
- **Clear Responsibility**: The ownership tree establishes unambiguous resource management
- **Flexible Dependencies**: The dependency graph allows practical service relationships
- **Orderly Lifecycle**: Coordinated startup/shutdown prevents cascading failures
- **Scalability**: Components can be distributed while maintaining clear relationships
- **Runtime Composition**: Child applications can be dynamically added and removed during execution
- **Isolation**: Applications can operate independently but cooperate when needed