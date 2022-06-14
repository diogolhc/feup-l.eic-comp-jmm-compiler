# Compilers Project

## GROUP: COMP2022-1A

| Name | Number | Grade | Contribution |
| -----| ------ | ----- | ------------ |
| Catarina Oliveira Pires | 201907925 | 20 | 25% |  
| Diogo Luís Henriques Costa | 201906731 | 20 | 25% |
| Francisco José Barbosa Marques Colino | 201905405 | 20 | 25% |
| Pedro Miguel Jesus da Silva | 201907523 | 20 | 25% |

GLOBAL Grade of the project: 20


## SUMMARY:

This tool compiles `J--` source files to Java bytecode.  

To compile and install the program, run ``gradle installDist``.  
After compiled, the program can be run using  ``comp2022-1A.bat`` (Windows) or ``comp2022-1A`` (Linux).

### Program invocation:
```
./comp2022-1a [-r=<num>] [-o] [-d] -i=<input_file.jmm>
```

Where:
- `-r` option directs the program to only use at most `<num>` local variables of the JVM.
    - If num = -1, it will use as many as needed.
    - If num =  0, it will use the minimum possible.
- `-o` option directs the program to perform code optimizations.
- `-d` option enables additional output related to the compilation process.
- `-i` is used to pass the path to the `J--` source file to compile.

The program will parse the input arguments to check if they are valid.  

If so, it will continue to parse the ``input_file.jmm``, build the symbol table,
perform semantic analysis, optimize at the AST level if option `-o` was used,
generate intermediate code (OLLIR), perform register allocation if option `-r` was used and
generate JVM code. Then it proceeds to save in the ``generated-files`` directory the `.j` file
with the Jasmin code, and a compiled `.class` file.

Note that at any stage, if errors are detected the compilation process aborts and the error is
displayed.


## SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)

### Symbol Table 
	
- Has information about imports and the declared class 
- Has information about extends, fields and methods 		
- Has information about the parameters and local variables of each method


### Type Verification 
	
- Verify if variable names used in the code have a corresponding declaration, either as a local variable, a method parameter or a field of the class (if applicable).
- Operands types of an operation must be compatible with the operation (e.g. int + boolean is an error because + expects two integers.) 
- Array cannot be used in arithmetic operations (e.g. array1 + array2 is an error) 	
- Array access is done over an array 	
- Array access index is an expression of type integer 	
- Type of the assignee must be compatible with the assigned (an_int = a_bool is an error) 
- Expressions in conditions must return a boolean (if(2+3) is an error) 

### Function Verification 

- When calling methods of the class declared in the code, verify if the types of arguments of the call are compatible with the types in the method declaration 
- In case the method does not exist, verify if the class extends another class and report an error if it does not. Assume the method exists in one of the super classes, and that is being correctly called 	
- When calling methods that belong to other classes other than the class declared in the code, verify if the classes are being imported 

## CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)




## PROS: (Identify the most positive aspects of your tool)




## CONS:

We didn't sanitize variable names that are reserved in OLLIR like "array".
