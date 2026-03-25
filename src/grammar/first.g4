grammar first;

prog: stat* EOF ;

stat
    : expr #expr_stat
    | IF_kw '(' cond=expr ')' then=block ('else' else=block)? #if_stat
    | '>' expr #print_stat
    | functionDef #function_def_stat
    ;

functionDef
    : DEF_kw ID '(' paramList? ')' body=block
    ;

paramList
    : ID (',' ID)*
    ;

argList
    : expr (',' expr)*
    ;

block
    : stat #block_single
    | '{' stat* '}' #block_real
    ;

expr
    : l=expr op=(MUL|DIV) r=expr #binOp
    | l=expr op=(ADD|SUB) r=expr #binOp
    | ID '(' argList? ')' #funcCall
    | ID #id_tok
    | INT #int_tok
    | '(' expr ')' #pars
    | <assoc=right> ID '=' expr #assign
    ;

DEF_kw : 'def' ;
IF_kw  : 'if' ;

DIV : '/' ;
MUL : '*' ;
SUB : '-' ;
ADD : '+' ;

NEWLINE : [\r\n]+ -> channel(HIDDEN);
WS : [ \t]+ -> channel(HIDDEN);

INT : [0-9]+ ;
ID  : [a-zA-Z_][a-zA-Z0-9_]* ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;