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


## SEMANTIC ANALYSIS:

### Symbol Table 
	
- Has information about imports and the declared class 
- Has information about extends, fields and methods 		
- Has information about the parameters and local variables of each method


### Type Verification 
	
- Verify if variable names used in the code have a corresponding declaration, either as a local variable, a method parameter or a field of the class (if applicable)
- Declarations must be of a known type or class
- Operand types of an operation must be compatible with the operation (e.g. int + boolean is an error because + expects two integers) 
- Array cannot be used in arithmetic operations (e.g. array1 + array2 is an error) 	
- Array access is done over an array 	
- Array access index is an expression of type integer
- Array can only be of type integer
- Array size on initialization must be of type integer 	
- Type of the assignee must be compatible with the assigned (an_int = a_bool is an error) 
- Expressions in conditions must return a boolean (if(2+3) is an error) 

### Function Verification 

- When calling methods of the class declared in the code, verify if the types of arguments of the call are compatible with the types in the method declaration 
- In case the method does not exist, verify if the class extends another class and report an error if it does not. Assume the method exists in one of the super classes, and that is being correctly called 	
- When calling methods that belong to other classes other than the class declared in the code, verify if the classes are being imported 
- Length can only be called on arrays
- Literals can't call methods

### Main Verification

- Reference variable *this* can't be used in main

## CODE GENERATION:

### Abstract Syntax Tree

The source code is parsed accordingly to the grammar defined in
[JmmGrammar.jj](javacc/JmmGrammar.jj).  
From the parsing stage, results an AST with several annotations such as name
of ids and values of literals.


#### Optimizations (-o)

After the AST is generated, and before OLLIR is generated, three visitors are executed
in order while they are changing the AST:
1. [ConstPropagationVisitor.java](src/pt/up/fe/comp/ollir/optimization/optimizers/ConstPropagationVisitor.java)
2. [ConstFoldingVisitor.java](src/pt/up/fe/comp/ollir/optimization/optimizers/ConstFoldingVisitor.java)
3. [DeadCodeEliminationVisitor.java](src/pt/up/fe/comp/ollir/optimization/optimizers/DeadCodeEliminationVisitor.java)

The first substitutes local variables that can be replaced by constant values.  
The second performs constant folding in `not` operations and`binary` operations, including short-cut evaluation of
the `&&` operation.
The third checks if `if` and `while` conditions are immediate values and if so eliminates the code accordingly.
Note that in the case of `<` condition, if its operands are the same id, the condition evaluates to false, and
dead code can be eliminated.

Also additional care is taken in order to detect the value of while loop conditions at the start of execution,
so that during OLLIR generation the while loop can be promoted to a do-while, avoiding the need of a goto instruction
and a label.

This is done by duplicating the while condition in here before the optimization loop:
[WhileConditionDuplicatorVisitor.java](src/pt/up/fe/comp/ollir/optimization/optimizers/WhileConditionDuplicatorVisitor.java)  
And after checking its value:
[DoWhileAnnotatorVisitor.java](src/pt/up/fe/comp/ollir/optimization/optimizers/DoWhileAnnotatorVisitor.java)

The rationale for choosing this particular order of optimizations is that if we first propagate
constants on the expressions and then fold them, we can detect ifs and whiles whose conditions
allow inlining/deleting them.

Example:

Before:
```
public int optimizeThis(boolean a) {
    boolean b;
    boolean c;
    boolean d;
    
    b = false;
    c = false;
    d = true;

    // b's value was propagated
    if (b) {
        io.println(0);
    } else {
        io.println(1);
    }

    // a's value is not known but we can still 
    // simplify with short-cut evaluation
    if ((c && d) && a) {
        io.println(2);
    } else {
        io.println(3);
    }

    return 0;
}
```
after:
```
public int optimizeThis(boolean a) {
    boolean b;
    boolean c;
    boolean d;
    
    b = false;
    c = false;
    d = true;

    // The ifs were removed and the elses were enlined 
    io.println(1);
    io.println(3);

    return 0;
}
```

### OO-based Low-Level Intermediate Representation (OLLIR)

OLLIR code is generated by visiting the AST. This intermediate representation translates
the AST to a 3 address code representation. This translation is performed by
[OllirGenerator.java](src/pt/up/fe/comp/ollir/OllirGenerator.java).


#### Optimizations (-o)

During the generation of OLLIR, elimination of unnecessary GOTOs is performed accordingly to the annotation generated as
previously stated in while loops.  

#### Optimization (-r)
With the -r option the user can limit the maximum number of local variables a method may use.
The optimization applies a graph colouring heuristic on the interference graph (at [LocalVariableInterferenceGraph.java](src/pt/up/fe/comp/ollir/optimization/LocalVariableInterferenceGraph.java)
) to allocate the variables to JVM local
variables. This graph is created by performing a liveness analysis at [LivenessAnalyser.java](src/pt/up/fe/comp/ollir/optimization/LivenessAnalyser.java) on the control flow graph.
If the algorithm cannot colour the graph with the provided limit, the compiler reports an error with the required number
of local variables.

The optimization is performed at [LocalVariableOptimization.java](src/pt/up/fe/comp/ollir/optimization/LocalVariableOptimization.java).

Example:

The code:
```
public int soManyRegisters(int arg){
   int a;
   int b;
   int c;
   int d;
   a = 0;
   b = a;
   c = b;
   d = c;
   return d;
}
```

The JVM generated without the optimization:
```
// Using as many local variables as possible
.method public soManyRegisters(I)I
	.limit stack 1
	.limit locals 6
	iconst_0
	istore_2
	iload_2
	istore_3
	iload_3
	istore 4
	iload 4
	istore 5
	iload 5
	ireturn
.end method
```

The JVM generated with the optimization:
```
// With just one local variable the value 0 can be propagated to the return
.method public soManyRegisters(I)I
	.limit stack 1
	.limit locals 3
	iconst_0
	istore_2
	iload_2
	istore_2
	iload_2
	istore_2
	iload_2
	istore_2
	iload_2
	ireturn
.end method
```


### JASMIN

Jasmin code is generated from the OLLIR code in [JasminBackender.java](src/pt/up/fe/comp/jmm/jasmin/JasminBackender.java).  
Everything is properly translated. Also stack limits and local limits are being calculated.


#### Optimizations

At this level, instruction selection is performed:
- x = x + `<const>` => iinc x `<const>`
- if (x < 0) => iflt
- if (0 < x) => ifgt
- if (!x) => ifeq
- if (x) => ifne
- if (x < y) => if_icmplt

Also, load and store instructions were selected:  
e.g. iload_0, bipush 10, iconst_0...

## PROS:

- Everything proposed was implemented, including both -o and -r optimizations as described above.  
- The [-d] option is very verbose which is very useful when trying to understand the compilation process.  

- Shortcut evaluation was performed on `&&` as shown in this test:
[ShortCutAnd.jmm](test/fixtures/public/selfMade/ShortCutAnd.jmm).  

- The compiler is highly user-friendly, giving useful information on syntactic and semantic error reports such as line, column and readable
messages.

- Many optimizations were implemented:
  - Constant propagation
  - Use of optimized instructions
  - Constant folding (with also shortcut evaluation was performed on `&&` as shown in this test:
    [ShortCutAnd.jmm](test/fixtures/public/selfMade/ShortCutAnd.jmm))
  - Dead code elimination
  - If in-lining

## CONS:

- We didn't sanitize variable names that are reserved in OLLIR like "array".
