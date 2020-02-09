# MicroJava++ :penguin:
> Implementation of the programming language compiler. Based on the Truffle framework. MicroJava as the base grammar for MicroJava++

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/built-with-science.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/built-with-resentment.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/pretty-risque.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/you-didnt-ask-for-this.svg)](https://forthebadge.com)

## Table of contents
 - [Language description](#language-description)
   - [Grammar](#grammar)
   - [Extensions](#extensions-moneybag)
   - [Not implemented yet](#not-implemented-yet-fast_forward)
 - [Team](#team-bulb)
 
## Language description
 ### Grammar
 *Also known as __MicroJava - Grammatik__* :alien:
 ```
 Program = "​program​" ​ident​ ​{ ​ConstDecl ​| ​VarDecl ​| ​ClassDecl ​} "​{​" ​{​MethodDecl​}​ "​}​". 
 
 ConstDecl = "​final​" ​Type​ ​ident​ "​=​" ​( ​number ​| ​charConst ​)​ "​;​". 
 
 VarDecl = Type​ ​ident​ ​{ ​"​,​" ​ident ​}​ "​;​". 

 ClassDecl = "​class​" ​ident​ "​{​" ​{ ​VarDecl ​} ​"​}​". 
 
 MethodDecl = ( T ​ ype ​| ​"​void​" ​)​ ​ident​ "​(​" ​[ ​FormPars ​]​ "​)​" { V ​ arDecl ​}​ ​Block​. 
 
 FormPars = Type​ ​ident​ ​{ ​"​,​" ​Type​ ​ident ​}​. Type = ident​ ​[ ​"​[​" "​]​" ​]​. 
 
 Block = "​{​" ​{ ​Statement ​}​ "​}​". 
 
 Statement = Designator​ ​( ​Assignop​ ​Expr ​|​ ​ActPars​ ​| ​"​++​" ​| ​"​--​" ​)​ "​;​" 
             | "​if​" "​(​" ​Condition​ "​)​" ​Statement​ ​[ ​"​else​" ​Statement ​] 
             | "​while​" "​(​" ​Condition​ "​)​" ​Statement 
             | "​break​" "​;​" | "​continue​" "​;​" 
             | "​return​" ​[ ​Expr ​]​ "​;​" 
             | "​read​" "​(​" ​Designator​ "​)​" "​;​" 
             | "​print​" "​(​" ​Expr​ ​[ ​"​,​" ​number ​]​ "​)​" "​;​" 
             | Block 
             | "​;​". 
 
 Assignop = "​=​" ​| ​"​+=​" ​| ​"​-=​" ​| ​"​*=​" ​| ​"​/=​" ​| ​"​%=​". 
 
 ActPars = "​(​" ​[ ​Expr ​{ ​"​,​" ​Expr ​} ] ​"​)​". 
 
 Condition = CondTerm​ ​{ ​"​||​" ​CondTerm ​}​. 
 
 CondTerm = CondFact​ ​{ ​"​&&​" ​CondFact ​}​. 
 
 CondFact = Expr​ ​Relop​ ​Expr​. Relop = "​==​" ​| ​"​!=​" ​| ​"​>​" ​| ​"​>=​" ​| ​"​<​" ​| ​"​<=​". 
 
 Expr = [ ​"​–​" ​]​ ​Term​ ​{ ​Addop​ ​Term ​}​. Term = Factor​ ​{ ​Mulop​ ​Factor ​}​. 
 
 Factor = Designator​ ​[ ​ActPars ​] 
          | number 
          | charConst 
          | "​new​" ​ident​ ​[ ​"​[​" ​Expr​ "​]​" ​]
          | "​(​" ​Expr​ "​)​". 
          
 Designator = ident​ ​{ ​"​.​" ​ident ​| ​"​[​" ​Expr​ "​]​" ​}​.
 
 Addop = "​+​" ​| ​"​–​". 
 
 Mulop = "​*​" ​| ​"​/​" ​| ​"​%​". 
 ```
 ---
  ### Extensions :moneybag:
  #### MicroJava++ supports additional features, not sated in the grammar:
  - [x] Scoping (Functions\` and blocks\` scopes, we are very proud of this! :raised_hands:);
  - [x] Declaring variables inside of funtions/blocks;
  - [x] Declaring constant variables inside the functions/blocks;
  - [x] Constant variables can be initialized with expression, not only literal (inside of the block, global variables suport only literal initilization);
  - [x] Supportied types: __*Integer*__, __*Boolean*__, __*Character*__, __*Double*__;
  ---
  ### Not implemented yet :fast_forward:
  - [ ] *Nodes for arrays were implementes, but to work corectly with scoping syste, there is still work to be done;*
  - [ ] __*TO BE DONE*__;
  ---
  ## Team :bulb:
  | [Roman Milishchuck](https://github.com/RomanMilishchuk) | [Hermann Yavorskyi](https://github.com/wardady) |
  | :---: | :---: |
  | <img src="https://i.imgur.com/FtFe4GD.gif"> | <img src="https://66.media.tumblr.com/dd21357585350785d3aa72c2306f1bbf/tumblr_osmh86FMB41qehrvso2_500.gifv"> |
|  __*` "I am starting to like Java... LoL, I Fooled you." `*__ |  :boom::skull:  |

---