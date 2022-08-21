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
### Maven
```xml
<dependency>
  <groupId>io.github.nowipi</groupId>
  <artifactId>JON</artifactId>
  <version>2.0-SNAPSHOT</version>
</dependency>
```
