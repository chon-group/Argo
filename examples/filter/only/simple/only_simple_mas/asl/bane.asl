/* Initial beliefs and rules */
{include("examples/argoConfigAgent.asl")}

/* Initial goals */

!start.

/* Plans */

+!start:
serialPort(Port) <-
	.print("Ah, Mr. Anderson, I see you are as predictable in this world as you are in the other.");
	.argo.port(Port);
	.argo.filter(only, ledStatus(X));
	.argo.percepts(open);
	.argo.limit(1000).

+ledStatus(on) <-
	.print("Turning OFF the Led in Arduino!");
	.argo.act(ledOff).

+ledStatus(off) <-
	.print("Turning ON the Led in Arduino!");
	.argo.act(ledOn).

+port(Port,Status):
Status = off | Status = timeout <-
	.argo.percepts(close);
	.print("It's not over, Mr. Anderson! It's not over!").
