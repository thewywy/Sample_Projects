# Generic Recursive Descent Compiler by Wyatt Sorenson

Requirements: Windows and Java  

Main Source Code:  
Compiler: Driver.java  
Assembler/VirtualMachine: vm.cpp  

How to use:  

click_to_compile_assemble_and_execute.bat does the following:  

1. 	executes Compiler.jar  

	passing source_code_to_be_compiled.txt  

	generates assembly_output.asm  


2. 	executes virtual_machine_to_convert_to_binary_and_execute.exe  

	passing  assembly_output.asm  

	assembles assembly_output.asm code into machine code  

	executes machine code virtually  


Uncomment source_code_to_be_compiled.txt lines to view syntax and semantic error catching.  