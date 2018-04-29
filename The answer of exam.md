### Theme 1
##### a. Define reliability and availability using you own words.
Answer:

##### b. Reliability can be achieved by (i) preventing, (ii) removing or (iii) tolerating faults. describe with examples how this can be done for att least two of the (i)-(iii)
Answer:

### Theme 2
##### a. Describe at least two major differences in terms of critical system attributes between synchronous and non-synchronous distributed systems?
synchronous distributed systems means that there are upper bounds on response times, delays etc. Asynchronous distributed systems means that there are no upper bound on times. Since dependability is based on detecting omissions and temporal failures based on timing, it is dependent on that the distributed system is synchronous. *

##### b. Describe briefly one of the most common transparency properties for distributed systems.
Access transparancy: The user does not need to know which computer he access, whether local or remote.

### Theme 3
##### a.
###### i.Describe one service that operating systems must support to enable development of distributed systems.
Answer:

###### ii: Why is it possible to use eventual consistency in naming services?
Answer:

###### iii: Describe one problem that is handled by employing cryptography and authentication in distributed systems.
Answer:

### Theme 4
##### a. Describe one goal with interprocess communication and one risk associated with achieving this goal.
To transfer the message between process with standard structure of messaages. The risk of that is some messages is too large to fit the structure.

##### b. Describe one problem that is solved by using rpc instead of using udp/ip or tcp/ip and one risk associated with solving this problem.
With using rpc it will be more clear for each command and wukk ve easier to solve the problem of wrong command by using rpc. The risk is it reduce the flexibility of content of message.

###Theme 5
##### a. What problem does election algorithms try to solve? why is this important in distributed systems?
The algorithm try to solve the problem when the primary replica management crashed. Because we need to make sure the primary do his job well and have all the previous information to conmmunicate with external front end, so it is important.

##### b. Provide two examples describing under what circumstances are logical clocks useful? Motivate you answer.
Answer:

### Theme 6
##### a. Describe what distributed file systems are used for as well as a critical issue regarding providing a distributed file systems service.
The distributed file system are used for fetching, reading, writing a file in distributed system. The issue of providing a file system server is the concurrency of the writing operation. and when you done with editing a file, how to synchronized it to the system.

##### b. Please explain the oncepts IaaS, PaaS, and SaaS
- IaaS: means infrastructure as a service. Which mean the vendor will provide hardware etc. to the user. such as virtual machine and virtual disk.
- Paas: means platform as a service. Which mean the vendor will provide the platform as the service. user can build whatever they want on it.
- SaaS: means software as a service. Which mean that the vendor will provide the software for user. such as email service etc.
	
### Theme 7
##### a. Describe the basic architecture of fault-tolerant services masking permanent and temporay omission failures (crash of components in the architecture or loss of messages). What is the roles of each component and a critical requirement for each component?
Answer: 



