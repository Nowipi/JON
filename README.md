# JON
Java Object Notation is an alternative to JSON with features like object refrences

## Usage/Examples

### Create JON
```java
JON jon = new JON();

ExampleObject obj = new ExampleObject();

String jon = jon.toJON(obj);

```
### Read JON
```java
JON jon = new JON();

//jon is your jon String
ExampleObject obj = jon.fromJON(jon, ExampleObject.class);

```
