package com.intellij.idea.plugin.hybris.impex;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.CustomHighlighterTokenType;

%%

%class ImpexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
    return;
%eof}

crlf        = (([\n])|([\r])|(\r\n))
white_space = [ \t\f]

end_of_line_comment_marker = [#]
end_of_line_comment_body   = [^\r\n]*

bean_shell_marker = [#][%]

single_string = ['](('')|([^'\r\n])*)[']
// Double string can contain line break
double_string = [\"](([\"][\"])|[^\"])*[\"]

macro_declaration = [$][:jletterdigit:]+
macro_usage       = [$][:jletterdigit:]+
macro_value       = (({double_string})|([^\r\n]*))*

left_square_bracket  = [\[]
right_square_bracket = [\]]

left_round_bracket  = [\(]
right_round_bracket = [\)]

semicolon    = [;]
comma        = [,]
assign_value = [=]

default_path_delimiter    = [:]
alternative_map_delimiter = [|]

boolean = (("true")|("false"))
digit   = [[:digit:]]+
class_with_package = ([:jletterdigit:]+[.][:jletterdigit:]+)+

parameter_name = ([:jletterdigit:]+[.]?[:jletterdigit:]+)+
alternative_pattern = [|]
special_parameter_name = [@][:jletterdigit:]+

attribute_name  = [:jletterdigit:]+
attribute_value = [^,:| \t\f\]\r\n]+

document_id = [&][:jletterdigit:]+

header_mode_insert        = "INSERT"
header_mode_update        = "UPDATE"
header_mode_insert_update = "INSERT_UPDATE"
header_mode_remove        = "REMOVE"

header_type = [:jletterdigit:]+

value_subtype      = [:jletterdigit:]+
field_value        = [^;,:| \t\f\r\n]+
field_value_ignore = "<ignore>"

%state COMMENT
%state WAITING_MACRO_VALUE
%state MACRO_DECLARATION
%state HEADER_TYPE
%state HEADER_LINE
%state FIELD_VALUE
%state BEAN_SHELL
%state MODYFIERS_BLOCK
%state WAITING_ATTR_OR_PARAM_VALUE
%state HEADER_PARAMETERS

%%

{crlf}                                                      { yybegin(YYINITIAL); return ImpexTypes.CRLF; }

{white_space}+                                              { return TokenType.WHITE_SPACE; }

<YYINITIAL> {
    {bean_shell_marker}                                     { yybegin(BEAN_SHELL); return ImpexTypes.BEAN_SHELL_MARKER; }

    {end_of_line_comment_marker}                            { yybegin(COMMENT); return ImpexTypes.COMMENT_MARKER; }

    {macro_declaration}                                     { yybegin(MACRO_DECLARATION); return ImpexTypes.MACRO_DECLARATION; }

    {header_mode_insert}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_INSERT; }
    {header_mode_update}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_UPDATE; }
    {header_mode_insert_update}                             { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_INSERT_UPDATE; }
    {header_mode_remove}                                    { yybegin(HEADER_TYPE); return ImpexTypes.HEADER_MODE_REMOVE; }

    {value_subtype}                                         { yybegin(FIELD_VALUE); return ImpexTypes.VALUE_SUBTYPE; }
    {semicolon}                                             { yybegin(FIELD_VALUE); return ImpexTypes.FIELD_VALUE_SEPARATOR; }
}

<COMMENT> {
    {end_of_line_comment_body}                              { return ImpexTypes.COMMENT_BODY; }
}

<BEAN_SHELL> {
    {double_string}                                         { return ImpexTypes.BEAN_SHELL_BODY; }
}

<FIELD_VALUE> {
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }
    {field_value_ignore}                                    { return ImpexTypes.FIELD_VALUE_IGNORE; }
    {boolean}                                               { return ImpexTypes.BOOLEAN; }
    {digit}                                                 { return ImpexTypes.DIGIT; }
    {class_with_package}                                    { return ImpexTypes.CLASS_WITH_PACKAGE; }

    {comma}                                                 { return ImpexTypes.FIELD_LIST_ITEM_SEPARATOR; }
    {default_path_delimiter}                                { return ImpexTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }

    {field_value}                                           { return ImpexTypes.FIELD_VALUE; }
    {semicolon}                                             { return ImpexTypes.FIELD_VALUE_SEPARATOR; }
}

<HEADER_TYPE> {
    {header_type}                                           { yybegin(HEADER_LINE); return ImpexTypes.HEADER_TYPE; }
}

<HEADER_LINE> {
    {semicolon}                                             { return ImpexTypes.PARAMETERS_SEPARATOR; }
    {comma}                                                 { return ImpexTypes.COMMA; }

    {macro_usage}                                           { return ImpexTypes.MACRO_USAGE; }
    {document_id}                                           { return ImpexTypes.DOCUMENT_ID; }
    {parameter_name}                                        { return ImpexTypes.HEADER_PARAMETER_NAME; }
    {alternative_pattern}                                   { return ImpexTypes.ALTERNATIVE_PATTERN; }
    {special_parameter_name}                                { return ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME; }
    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpexTypes.ASSIGN_VALUE; }

    {left_round_bracket}                                    { return ImpexTypes.ROUND_BRACKETS; }
    {right_round_bracket}                                   { return ImpexTypes.ROUND_BRACKETS; }

    {left_square_bracket}                                   { yybegin(MODYFIERS_BLOCK); return ImpexTypes.SQUARE_BRACKETS; }
    {right_square_bracket}                                  { return ImpexTypes.SQUARE_BRACKETS; }
}

<MODYFIERS_BLOCK> {
    {attribute_name}                                        { return ImpexTypes.ATTRIBUTE_NAME; }

    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpexTypes.ASSIGN_VALUE; }

    {single_string}                                         { return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpexTypes.DOUBLE_STRING; }

    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpexTypes.SQUARE_BRACKETS; }

    {comma}                                                 { return ImpexTypes.ATTRIBUTE_SEPARATOR; }

    {alternative_map_delimiter}                             { yybegin(MODYFIERS_BLOCK); return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
}

<WAITING_ATTR_OR_PARAM_VALUE> {
    {boolean}                                               { yybegin(MODYFIERS_BLOCK); return ImpexTypes.BOOLEAN; }
    {digit}                                                 { yybegin(MODYFIERS_BLOCK); return ImpexTypes.DIGIT; }
    {single_string}                                         { yybegin(MODYFIERS_BLOCK); return ImpexTypes.SINGLE_STRING; }
    {double_string}                                         { yybegin(MODYFIERS_BLOCK); return ImpexTypes.DOUBLE_STRING; }
    {class_with_package}                                    { yybegin(MODYFIERS_BLOCK); return ImpexTypes.CLASS_WITH_PACKAGE; }
    {default_path_delimiter}                                { yybegin(MODYFIERS_BLOCK); return ImpexTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { yybegin(MODYFIERS_BLOCK); return ImpexTypes.ALTERNATIVE_MAP_DELIMITER; }
    {macro_usage}                                           { yybegin(MODYFIERS_BLOCK); return ImpexTypes.MACRO_USAGE; }
    {attribute_value}                                       { yybegin(MODYFIERS_BLOCK); return ImpexTypes.ATTRIBUTE_VALUE; }
    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpexTypes.SQUARE_BRACKETS; }
}

<MACRO_DECLARATION> {
    {assign_value}                                          { yybegin(WAITING_MACRO_VALUE); return ImpexTypes.ASSIGN_VALUE; }
}

<WAITING_MACRO_VALUE> {
    {macro_value}                                           { return ImpexTypes.MACRO_VALUE; }
}

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
