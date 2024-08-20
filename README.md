# Argo
[![](https://jitpack.io/v/chon-group/argo.svg)](https://jitpack.io/#chon-group/argo)

Argo is a customized agent architecture built on the Jason framework that extends Jason’s standard agents by adding the ability to control microcontrollers. Argo agents interact with the physical environment, capturing information from the environment using sensors and acting by sending commands to the microcontroller to activate the actuators. Sensors’ information is automatically processed as perceptions in the agent’s belief base. 

### Using ARGO with JaCaMo 
+ Go to [Argo-JCM Package](https://github.com/chon-group/argo-jcm)

## Creating _Hello Argo_ with Jason Framework
###	Dependencies
First, you need to deploy the Javino Blink example on your Arduino Board. Follow the steps below:

+ Download the latest [Javino Library](https://github.com/chon-group/javino2arduino/archive/refs/tags/javino-latest.zip).
+ Import the library into your Arduino IDE.
+ Create a new project with the Javino Blink example.
![arduino](https://github.com/chon-group/argo-jcm/assets/32855001/d5be0497-de49-46ab-8da5-f86c9db4a1da)
+ Deploy the code on your Arduino Board.


### Creating the Jason Project
To install Jason-CLI follow these instructions: [Jason-CLI Installation](https://github.com/chon-group/dpkg-jason).

In a terminal, execute the following command:
```sh
jason app create helloArgo --console
```

#### Including the Argo Package in the Jason Project
Download the Argo .jar into lib folder as folows:
```sh
wget https://github.com/chon-group/argo/raw/master/out/argo.jar -P helloArgo/lib/
```

Edit  the project file __helloArgo/helloArgo.mas2j__  as follows:

```
MAS helloArgo {
    agents: bob agentArchClass jason.Argo;
    aslSourcePath: "src/agt";
}
```

Edit the agent file __helloArgo/src/agt/bob.asl__ as follows:
```sh
/* Initial beliefs and rules */
serialPort(ttyACM0).

/* Initial goals */
!start.

/* Plans */
+!start:
serialPort(Port) <- 
	.print("Ah, Mr. Anderson, I see you are as predictable in this world as you are in the other.");
	argo.port(Port);
	argo.percepts(open).

+ledStatus(on) <-
	.print("Turning ON  the Led in Arduino!");
	argo.act(ledOff).

+ledStatus(off) <-
	.print("Turning OFF the Led in Arduino!");
	argo.act(ledOn).

+port(Port,Status):
Status = off | Status = timeout <-
	argo.percepts(close);
	.print("It's not over, Mr. Anderson! It's not over!").
```

### Running
```sh
sudo chmod 777 /dev/ttyACM0
jason helloArgo/helloArgo.mas2j
```
![argoJason](https://github.com/chon-group/argo/assets/32855001/5d7a6110-3dd2-4d05-8ae2-c995357e1baa)

## Perceptions Filter

ARGO agents have a perception filter mechanism that allows the selection of the perceptions that should be captured from the environment.

An internal action called .filter was created to use the perception filters, and it has one mandatory parameter and two optional parameters.
The internal action follows the pattern: 

.<span style="color:green">*filter*</span>(<span style="color:blue">***FILTERING_ACTION***</span>, <span style="color:orange">***PERCEPTIONS_TO_BE_FILTERED***</span>, <span style="color:orange">***CONDITION***</span>). So:
- The name of the internal action is: <span style="color:green">*.filter*</span>;
- The mandatory parameter is: <span style="color:blue">***FILTERING_ACTION***</span>.
- The first optional parameter is: <span style="color:orange">***PERCEPTIONS_TO_BE_FILTERED***</span>.
- The second optional parameter is: <span style="color:orange">***CONDITION***</span>.

The filtering actions are three until now:
- except;
- only;
- remove;

### Except

The **except** action allows the filtering of all perceptions except the perceptions specified.

#### Example

For the perception filter examples, we are using an ARGO agent called Bane that controls an Arduino with a connected LED.

Bane captures the following perceptions of the environment:
- ledStatus(off) - Indicating that the LED is Off.
- ledStatus(on) - Indicating that the LED is On.
- port(ttyUSB0,on) - Indicating that the communication port with the Arduino is On.
- port(ttyUSB0,off) - Indicating that the communication port with the Arduino is Off.
- resourceName(myArduino) - Indicating the Arduino name.

The **except** filtering action allows us some filtering configuration possibilities:
1. Simple configuration:
```
.argo.filter(except, ledStatus(X));
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **not** be filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)
- ledStatus(on)

2. Configuration with condition: 
```
.argo.filter(except, ledStatus(X), X = off);
```

With this code, only the perceptions **ledStatus(off)** will **not** be filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)

3. Configuration with dont care:
```
.argo.filter(except, ledStatus(__));
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **not** be filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)
- ledStatus(on)

4. Configuration with a list:
```
.argo.filter(except, [ledStatus(off), ledStatus(on)]);
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **not** be filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)
- ledStatus(on)

### Only

The **only** action allows the filtering only the specified perceptions.

#### Example

For the perception filter examples, we are using the same ARGO agent called Bane.

The **only** filtering action allows us some filtering configuration possibilities:
1. Simple configuration:
```
.argo.filter(only, ledStatus(X));
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **be** filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- port(ttyUSB0,on)
- port(ttyUSB0,off)
- resourceName(myArduino)


2. Configuration with condition:
```
.argo.filter(only, ledStatus(X), X = off);
```

With this code, only the perceptions **ledStatus(off)** will **be** filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(on)
- port(ttyUSB0,on)
- port(ttyUSB0,off)
- resourceName(myArduino)

3. Configuration with dont care:
```
.argo.filter(only, ledStatus(__));
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **be** filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- port(ttyUSB0,on)
- port(ttyUSB0,off)
- resourceName(myArduino)

4. Configuration with a list:
```
.argo.filter(only, [ledStatus(off), ledStatus(on)]);
```

With this code, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **be** filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- port(ttyUSB0,on)
- port(ttyUSB0,off)
- resourceName(myArduino)

### Remove

The **remove** action allows us to remove all filter configurations.

#### Example

For the perception filter examples, we are using the same ARGO agent called Bane.

The **remove** filtering action allows us only one filtering configuration possibility:

```
.argo.filter(except, ledStatus(X));
.argo.filter(only, ledStatus(X));
.argo.filter(remove);
```
With this code, in the first line, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **not** be filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)
- ledStatus(on)

In the second line, only the perceptions **ledStatus(off)** and **ledStatus(on)** will **be** filtered/blocked.
So, in Agent Bane's mind, the following perceptions will arrive:
- Nothing.

However, in the last line, the filtering action **remove** is used, removing all filter configurations.
So, in Agent Bane's mind, the following perceptions will arrive:
- ledStatus(off)
- ledStatus(on)
- port(ttyUSB0,on)
- port(ttyUSB0,off)
- resourceName(myArduino)

**NOTE:** This repository has a directory called "examples" that contains examples of the Bane agent executing each of the perception filter configurations presented.

## COPYRIGHT
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />Argo is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>. The licensor cannot revoke these freedoms as long as you follow the license terms:

* __Attribution__ — You must give __appropriate credit__ like below:

Pantoja, C.E., Stabile, M.F., Lazarin, N.M., Sichman, J.S. (2016). ARGO: An Extended Jason Architecture that Facilitates Embedded Robotic Agents Programming. In: Baldoni, M., Müller, J., Nunes, I., Zalila-Wenkstern, R. (eds) Engineering Multi-Agent Systems. EMAS 2016. Lecture Notes in Computer Science(), vol 10093. Springer, Cham. [https://doi.org/10.1007/978-3-319-50983-9_8](https://www.researchgate.net/publication/311692258_ARGO_An_Extended_Jason_Architecture_that_Facilitates_Embedded_Robotic_Agents_Programming)

```
@InProceedings{10.1007/978-3-319-50983-9_8,
	author="Pantoja, Carlos Eduardo and Stabile, M{\'a}rcio Fernando and Lazarin, Nilson Mori and Sichman, Jaime Sim{\~a}o",
	editor="Baldoni, Matteo and M{\"u}ller, J{\"o}rg P. and Nunes, Ingrid and Zalila-Wenkstern, Rym",
	title="{ARGO: An Extended Jason Architecture that Facilitates Embedded Robotic Agents Programming}",
	booktitle="Engineering Multi-Agent Systems",
	year="2016",
	publisher="Springer International Publishing",
	address="Cham",
	pages="136--155",
	isbn="978-3-319-50983-9"
}
```	

## EXTRA
Some papers that have used Argo agents to control Arduino microcontrollers 

* [Swapping Physical Resources at Runtime in Embedded MultiAgent Systems](https://doi.org/10.5220/0011750700003393);
* [Integrating Embedded Multiagent Systems with Urban Simulation Tools and IoT Applications](https://doi.org/10.22456/2175-2745.110837);
* [Bio-Inspired Protocols for Embodied Multi-Agent Systems](https://doi.org/10.5220/0010257803120320);
