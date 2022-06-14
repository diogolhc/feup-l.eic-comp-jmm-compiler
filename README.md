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




## CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)




## PROS: (Identify the most positive aspects of your tool)




## CONS:

We didn't sanitize variable names that are reserved in OLLIR like "array".
