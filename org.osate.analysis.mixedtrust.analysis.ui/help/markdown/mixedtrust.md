<!--
Copyright (c) 2004-2021 Carnegie Mellon University and others. (see Contributors file). 
All Rights Reserved.

NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
which is available at https://www.eclipse.org/legal/epl-2.0/
SPDX-License-Identifier: EPL-2.0

Created, in part, with funding and support from the United States Government. (see Acknowledgments file).

This program includes and/or can make use of certain third party source code, object code, documentation and other
files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
conditions contained in any such Third Party Software or separate license file distributed with such Third Party
Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party benefici-
aries to this license with respect to the terms applicable to their Third Party Software. Third Party Software li-
censes only apply to the Third Party Software and not any other portion of this program or this program as a whole.
-->
# Mixed Trust Scheduling

[TOC levels=2-4 bullet hierarchy]

This Mixed Trust Scheduling analysis attempts to schedule threads using the scheduling analysis described in ["Mixed-Trust Computing for Real-Time Systems"](https://ieeexplore.ieee.org/document/8864566/).  The analysis makes use of properties declared in the property set `Mixed_Trust_Properties` to describe the relationships among processors, guest operating systems, hypervisors, and mixed trust tasks.  The plug in provides the property set `Mixed_Trust_Properties` to the OSATE workspace; it can be found under the `Plug-in Contribuitions` heading inside an AADL project.  See the figure below.

![An example AADL project](images/ExampleProject.png)

## Running the Analysis

The analysis can be run over multiple models at the same time:

1. Select one or more working sets, projects, directories, or instance model `.aaxl` files in the `AADL Navigator`. 
2. Select `Analyses > Budget > Mixed Trust Scheduling` from the menu bar or navigator context menu.

The analysis finds all the instance models (`.aaxl` files) in the selected items and runs over each one.

*  _The analysis runs for each system operation mode in each model._  
* An output comma-separated-values (`.csv`) file is generated for each analyzed model.  The file is located in the `reports/MixedTrust` folder.  The file has the same name as the model file, but with `__MixedTrust` appended to the end.  (See in the example project above.)
* If the analysis finds inconsistencies, it will produce error or warning markers on the instance model file.  These errors and warnings are also described in the .CSV files.


## Using the Analysis

The analysis finds each processor in the instance model that is marked as a mixed trust processor (see below).  It then finds all the declared mixed trust tasks (see below) bound to each mixed trust processor.  The scheduling analysis is run for each mixed trust processor to determine if the tasks on that processor are schedulable, and if so, it reports the *E* value for each mixed trust task.

### Specfiying Mixed Trust Processors

A processor is declared to be a mixed trust processor by associating the processor with the property `Mixed_Trust_Properties::Mixed_Trust_Processor`.  The value of this property is a record of type of `Mixed_Trust_Properties::Mixed_Trust_Bindings`:

	Mixed_Trust_Bindings: type record (
		GuestOS: reference (virtual processor);
		HyperVisor: reference (virtual processor);
	);

Basically, the concepts of *guest operating system* and *hypervisor* are modeled by binding virtual processors to the processor.  This record value is used to declare which virtual processor is the guest operating system and which is the hypervisor.  The analysis generates error markers if

* both fields are not set.  (AADL semantics do not require this, but the analysis needs both values.)

* The referenced virtual processors are not *bound* to the processor to which the `Mixed_Trust_Processor` property is associated.

Here *bound* means an AADL binding either through an `Actual_Processor_Binding` property assocation on the virtual propcessor or by the virtual processor being a subcomponent of the processor.  The exaples below show both ways of doing things.

### Specifying Mixed Trust Tasks

A mixed trust task is made up of two threads: 

1. A thread that executes on the guest operation system.
2. A thread that executes on the hypervisor.

The overall mixed trust task has its own period, deadline, and *E* value.    The *E* value is actually calculated by the scheduling algorithm, so it is not required to be specified in the model. 

Mixed trust tasks are declared by a `Mixed_Trust_Properties::Mixed_Trust_Tasks` property association on the `system` component whose containment hierarchy contains the mixed the processor and the individual thread components.  The value of the property is a list of Mixed_Trust_Task records:

	Mixed_Trust_Task: type record (
		-- A task name to improve readability of the results
		Name: aadlstring;
		
		-- Period of the mixed trust task.  Periods of the referenced Guest and Hyper task threads should not be set
		Period: Time;
		-- Deadline of the mixed trust task.  Periods of the referenced Guest and Hyper task threads should not be set
		Deadline: Time;
		
		-- The Guest and Hyper task threads should be bound to different virtual processors representing
		-- the guest OS and hypervisor environments.  These virtual processors, in turn, must be bound to the
		-- physical processor;  
		
		-- Reference to the guest task thread.  Thread should only be referenced by a single mixed trust task
		GuestTask: reference (Thread);
		-- Reference to the hyper task thread.  Thread should only be referenced by a single mixed trust task
		HyperTask: reference (Thread);	
		
		-- The "E" Value of the mixed trust task.  Effectively the deadline of the guest task, but also
		-- duration of the time used in the hypervisor used to activate the hypertask if the guest task
		-- does not meet its deadline.
		E: Time;
	);

The `name` field is optional, but it helps to provide a human-readable name in the output of the analysis.  The `period` and `deadline` fields declare the period and deadline of the overall mixed trust task.  These values must be specified; the analysis generates error markers if they are not.  Fields `GuestTask` and `HyperTask` reference thread components that are the guest operation system task and hypervisor task, respecitively.  Finally, `E` allows the *E* value to be annoted on the model.  Again, this value is calculated by the analysis, it any specified value is ignored.  Currently the calculated value must be hand copied into the model.

#### More About Threads

The threads referenced by `GuestTask` and `HyperTask` must be bound to the correct virtual processors representing the guest operating system and hypervisor.  Again, by *bound* it is meant that thread components must have `Actual_Processor_Binding` property associations.  Furthermore, 

* The thread referenced by `GuestTask` must be bound to a virtual procoessor that is named as a `GuestOS` in a `Mixed_Trust_Processor` property association.  An error marker is generated otherwise.

* The thread referenced by `HyperTask` must be bound to a virtual procoessor that is named as a `HyperVisor` in a `Mixed_Trust_Processor` property association.  An error marker is generated otherwise.

* The two virtual processors must be bound to the same physical processor.  That is, the two threads making up the mixed trust task cannot be bound to different mixed trust processors: an error marker is generated if this is the case.

Each thread is expected to have a `Compute_Execution_Time` property association.  An error marker is generated if it does not.  This propety value is a range; the scheduler uses the maximum value.

Normally in AADL a thread is expected to have property associations for `Period` and `Deadline`.  In this case, however, the period and deadline is taken from the `Mixed_Trust_Task` record.  The analysis outputs warnings indicating that these values are not used when threads have period and deadline values.

## The CSV File

For each analyzed model, a comma-separated-values (`.csv`) file is generated in the `reports/MixedTrust` folder.  The file has the same name as the model file, but with `__MixedTrust` appended to the end.

The content is organized in a top-down manner, sorted in the following order

1. System operation modes (if any)
2. Physical buses
3. The virtual buses, broadcast groups, and connections bound to that bus, with virtual buses and broadcast groups being recursively output.

The output follows the general format, that for each system operation mode/bus/virtual bus/broadcast group, first a summary of the data (_capacity_, _budget_, _required budget_, and _actual_) for that level is displayed in tabular form, and then particular contained items are recursively visited.

Any warnings or errors associated with a bus/virtual bus/connection are output after the summary information for that item.

## Example

Here we analyze the system instance made from instantiating the classifier `Example::top.i` :

    package Example
        public
            with SEI;
    
        -- Some basics
    
        data D8
            properties
                Data_Size => 8 Bytes;
        end D8;
	
        -- buses
    
        bus MyBus
            properties
                Data_Size => 8 Bytes;
                SEI::BandWidthBudget => 512.0 KBytesps;
                SEI::BandWidthCapacity => 768.0 KBytesps;
        end MyBus;

        virtual bus MyVB1
            properties
                Data_Size => 16 Bytes; 
                SEI::BandWidthBudget => 384.0 KBytesps;
                SEI::BandWidthCapacity => 512.0 KBytesps;
        end MyVB1;
    
        virtual bus MyVB2
            properties
                Data_Size => 24 Bytes; 
                SEI::BandWidthBudget => 256.0 KBytesps;
                SEI::BandWidthCapacity => 384.0 KBytesps;
        end MyVB2;

        virtual bus MyVB3
            modes
                z1: initial mode;
                z2: mode;
            properties
		    	Data_Size => 32 Bytes in modes (z1), 64 Bytes in modes (z2);
			    SEI::BandWidthBudget => 128.0 KBytesps;
            SEI::BandWidthCapacity => 256.0 KBytesps;
        end MyVB3;
    
        system S1
            features
                out1: out data port D8;
                out2: out data port D8;
                out3: out data port D8;
                out4: out data port D8;
        end S1;
    
        system S2
            features
                in1: in data port D8;
                in2: in data port D8;
                in3: in data port D8;
                in4: in data port D8;
        end S2;

        -- assembled system
	
        system top
        end top;
	
        system implementation top.i
            subcomponents
                sub1: system s1;
                sub2: system s2;
                theBus: bus MyBus;
                VB1: virtual bus MyVB1;
                VB2: virtual bus MyVB2;
                VB3: virtual bus MyVB3;
            connections
                conn1: port sub1.out1 -> sub2.in1 {
                    Actual_Connection_Binding => (reference (theBus));
                    SEI::BandWidthBudget => 20.0 KBytesps;
                };
                conn2: port sub1.out2 -> sub2.in2 {
                    Actual_Connection_Binding => (reference (VB1));
                    SEI::BandWidthBudget => 40.0 KBytesps;
                };
                conn3: port sub1.out3 -> sub2.in3 {
                    Actual_Connection_Binding => (reference (VB2));
                    SEI::BandWidthBudget => 64.0 KBytesps;
                };
                conn4: port sub1.out4 -> sub2.in4 {
                    Actual_Connection_Binding => (reference (VB3));
                    SEI::BandWidthBudget => 96.0 KBytesps;
                };
            properties
                -- Bind the remaining virtual buses
                Actual_Connection_Binding => (reference (theBus)) applies to vb1;			
                Actual_Connection_Binding => (reference (vb1)) applies to vb2;			
                Actual_Connection_Binding => (reference (vb2)) applies to vb3;			
			
                -- Communication rates
                Communication_Properties::Output_Rate => [Value_Range => 800.0 .. 1000.0; Rate_Unit => PerSecond;] applies to sub1.out1;
                Communication_Properties::Output_Rate => [Value_Range => 800.0 .. 1000.0; Rate_Unit => PerSecond;] applies to sub1.out2;
                Communication_Properties::Output_Rate => [Value_Range => 800.0 .. 1000.0; Rate_Unit => PerSecond;] applies to sub1.out3;
                Communication_Properties::Output_Rate => [Value_Range => 800.0 .. 1000.0; Rate_Unit => PerSecond;] applies to sub1.out4;
        end top.i;
    end Example;


The system `top.i` has two subsystems that communicate across four connections.  There is one physical bus, and three layers of virtual buses.  Analysis operates over the following communication model:

* Bus `theBus`

    * Connection `conn1`
    * Virtual bus `VB1`
    
	    * Connection `conn2`
	    * Virtual bus `VB2`
	    
	        * Connection `conn3`
	        * Virtual bus `VB4`
	        
	            * Connection `conn4`
	            
In addition, the bus `VB3` (from classifier `MyVB3`) has two modes, giving the overall system instance two system operation modes.

Analysis gives a single error on the fourth connection in the system operation mode `(VB3.z2)`.  This error marker is visible in the `Problems` view:

![Screen shot of OSATE after analysis.](images/OSATE_problems.png)

The image below shows output file `Example_top_i_Instance__BusLoad.csv` opened in Excel.  Note how the output order follows the structure of communication model above.  

![Output file opened in Excel.](images/Excel.png)

## Invoking Programmatically

The analysis can be invoked programmatically by other tools by calling the method

        AnalysisResult invoke(IProgressMonitor, SystemInstance)

on an instance of the class `NewBusLoadAnalysis` in the package `org.osate.analysis.resource.budgets.busload`.  This is found in the plug-in `org.osate.analysis.resource.budgets`.

As the signature indicates, the method takes a possibly-`null` progress monitor, and the `SystemInstance` object of the model to analyze.  All the system operation modes of the model are analyzed.

A new instance of the class `NewBusLoadAnalysis` should be used for each system instance.   

### Result format

The format for the `AnalysisResult` tree returned by `invoke()` is as follows: 

`AnalysisResult`

* `analysis` = "Bus Load"
* `modelElement` = `SystemInstance` being analyzed
* `resultType` = `SUCCESS`
* `message = `"Bus load analysis of _name of system instance_"`
* `diagnostics` = _empty list_
* `parameters` = _empty list_
* `results` = one `Result` for each system operation mode

    * `modelElement` = `SystemOperationMode` instance object
    * `resultType` = `SUCCESS`
    * `message` = `""` if the SOM is `null` or the empty som, otherwise `"(xxx, ..., yyy)"`
    * `values` = _empty list_
    * `diagnostics` = _empty list_
    * `subResults` = one `Result` for each `ComponentInstance` with `category` of `Bus`
    
        * `modelElement` = `ComponentInstance` instance object
        * `resultType` = `SUCCESS`
        * `message` = The component's name from `getName()`
        * `values[0]` = The capacity of the bus in KB/s as specified by the `SEI::BandwidthCapacity` property (`RealValue`)
        * `values[1]` = The budget of the bus in KB/s as specified by the `SEI::BandwidthBudget` property (`RealValue`)
        * `values[2]` = The required budget of the bus in KB/s (the sum of the budgets of all the bound buses and connections) (`RealValue`)
        * `values[3]` = The actual usage of the bus in KB/s (the sum of the actual usages of all the bound buses and connections) (`RealValue`)
        * `values[4]` = The number of virtual buses bound to this bus (`IntegerValue`)
        * `values[5]` = The number of connections bound to this bus (`IntegerValue`)
        * `values[6]` = The number of broadcast sources bound to this bus (`IntegerValue`)
        * `values[7]` = The data overhead of the bus in bytes as computed by the analysis (`IntegerValue`)
        * `diagnostics` = Diagnostics associated with this bus.
        * `subResults` = indexes `0` through (`values[4]` - 1) refer to `Result` objects for virtual buses.

            * `Result` objects for virtual buses are the same as for buses

        * `subResults` = indexes `values[4]` through (`values[4]` + `values[5]` - 1) refer to `Result` objects for connections.  

            * `modelElement` = `ConnectionInstance` instance object
            * `resultType` = `SUCCESS`
            * `message` = The connection's name from `getName()`
            * `values[0]` = The budget of the connection in KB/s as specified by the `SEI::BandwidthBudget` property (`RealValue`)
            * `values[1]` = The actual usage of the bus in KB/s as computed by the multiplying the connection's data size by the connection's message rate.  This takes into account any messaging overhead by the bus hierarchy the connection is bound to.  (`RealValue`)
            * `diagnostics` = Diagnostics associated with this connection
            * `subResults` = _empty list_

        * `subResults` = indexes (`values[4]` + `values[5]`) through (`values[4]` + `values[5]` + `values[6]` - 1) refer to `Result` objects for broadcast sources.  

            * `modelElement` = `ConnectionInstanceEnd` instance object
            * `resultType` = `SUCCESS`
            * `message` = `"Broadcast from <src name>"` where _<src name>_ is the value of `modelElement.getInstanceObjectPath()`
            * `values[0]` = The budget of the broadcast source in KB/s (`RealValue`)
            * `values[1]` = The actual usage of the bus in KB/s (`RealValue`)
            * `diagnostics` = Diagnostics associated with this broadcast source
            * `subResults` = The `Result` objects for the connections that are part of the broadcast:

                * See above

