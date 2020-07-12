// This is a generated file. Not intended for manual editing.
package org.intellij.sdk.language.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.misset.opp.omt.psi.OMTTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class OMTParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return OMTFile(b, l + 1);
  }

  /* ********************************************************** */
  // item_*
  static boolean OMTFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OMTFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "OMTFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // variable | VALUE
  public static boolean inputArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputArgument")) return false;
    if (!nextTokenIs(b, "<input argument>", DOLLAR, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INPUT_ARGUMENT, "<input argument>");
    r = variable(b, l + 1);
    if (!r) r = consumeToken(b, VALUE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PARENTHESIS_OPEN (COMMA inputArgument | inputArgument)* PARENTHESIS_CLOSED
  public static boolean inputArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputArguments")) return false;
    if (!nextTokenIs(b, PARENTHESIS_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PARENTHESIS_OPEN);
    r = r && inputArguments_1(b, l + 1);
    r = r && consumeToken(b, PARENTHESIS_CLOSED);
    exit_section_(b, m, INPUT_ARGUMENTS, r);
    return r;
  }

  // (COMMA inputArgument | inputArgument)*
  private static boolean inputArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputArguments_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inputArguments_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inputArguments_1", c)) break;
    }
    return true;
  }

  // COMMA inputArgument | inputArgument
  private static boolean inputArguments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputArguments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inputArguments_1_0_0(b, l + 1);
    if (!r) r = inputArgument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA inputArgument
  private static boolean inputArguments_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputArguments_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && inputArgument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // model|modelBlockGroup|COMMENT|CRLF
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    boolean r;
    r = model(b, l + 1);
    if (!r) r = modelBlockGroup(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    return r;
  }

  /* ********************************************************** */
  // LISTITEM_BULLET variable
  public static boolean listItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listItem")) return false;
    if (!nextTokenIs(b, LISTITEM_BULLET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LISTITEM_BULLET);
    r = r && variable(b, l + 1);
    exit_section_(b, m, LIST_ITEM, r);
    return r;
  }

  /* ********************************************************** */
  // modelBlockId modelItem+
  public static boolean model(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "model")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockId(b, l + 1);
    r = r && model_1(b, l + 1);
    exit_section_(b, m, MODEL, r);
    return r;
  }

  // modelItem+
  private static boolean model_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "model_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelItem(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!modelItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "model_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (prefix+ | listItem+ | property+ | query+)+
  public static boolean modelBlockContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MODEL_BLOCK_CONTENT, "<model block content>");
    r = modelBlockContent_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!modelBlockContent_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelBlockContent", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // prefix+ | listItem+ | property+ | query+
  private static boolean modelBlockContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockContent_0_0(b, l + 1);
    if (!r) r = modelBlockContent_0_1(b, l + 1);
    if (!r) r = modelBlockContent_0_2(b, l + 1);
    if (!r) r = modelBlockContent_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // prefix+
  private static boolean modelBlockContent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefix(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!prefix(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelBlockContent_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // listItem+
  private static boolean modelBlockContent_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = listItem(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!listItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelBlockContent_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // property+
  private static boolean modelBlockContent_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = property(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!property(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelBlockContent_0_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // query+
  private static boolean modelBlockContent_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockContent_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = query(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!query(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelBlockContent_0_3", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (modelBlockId modelBlockContent) | (modelBlockId modelBlockGroup) | (modelBlockId PIPE scriptBlock?)
  public static boolean modelBlockGroup(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockGroup")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockGroup_0(b, l + 1);
    if (!r) r = modelBlockGroup_1(b, l + 1);
    if (!r) r = modelBlockGroup_2(b, l + 1);
    exit_section_(b, m, MODEL_BLOCK_GROUP, r);
    return r;
  }

  // modelBlockId modelBlockContent
  private static boolean modelBlockGroup_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockGroup_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockId(b, l + 1);
    r = r && modelBlockContent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // modelBlockId modelBlockGroup
  private static boolean modelBlockGroup_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockGroup_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockId(b, l + 1);
    r = r && modelBlockGroup(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // modelBlockId PIPE scriptBlock?
  private static boolean modelBlockGroup_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockGroup_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockId(b, l + 1);
    r = r && consumeToken(b, PIPE);
    r = r && modelBlockGroup_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // scriptBlock?
  private static boolean modelBlockGroup_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockGroup_2_2")) return false;
    scriptBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // KEY COLON
  public static boolean modelBlockId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelBlockId")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, COLON);
    exit_section_(b, m, MODEL_BLOCK_ID, r);
    return r;
  }

  /* ********************************************************** */
  // modelBlockId MODEL_ITEM_TYPE modelBlockGroup+
  public static boolean modelItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelItem")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockId(b, l + 1);
    r = r && consumeToken(b, MODEL_ITEM_TYPE);
    r = r && modelItem_2(b, l + 1);
    exit_section_(b, m, MODEL_ITEM, r);
    return r;
  }

  // modelBlockGroup+
  private static boolean modelItem_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modelItem_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modelBlockGroup(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!modelBlockGroup(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "modelItem_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // KEY EQUALS PREFIX_IRI
  public static boolean prefix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefix")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, EQUALS, PREFIX_IRI);
    exit_section_(b, m, PREFIX, r);
    return r;
  }

  /* ********************************************************** */
  // (KEY EQUALS VALUE) | (variable EQUALS VALUE)
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, "<property>", DOLLAR, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = property_0(b, l + 1);
    if (!r) r = property_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // KEY EQUALS VALUE
  private static boolean property_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, EQUALS, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  // variable EQUALS VALUE
  private static boolean property_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable(b, l + 1);
    r = r && consumeTokens(b, 0, EQUALS, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // QUERY_DEFINE VALUE inputArguments? QUERY_SEPARATOR queryStatement SEMICOLON
  public static boolean query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query")) return false;
    if (!nextTokenIs(b, QUERY_DEFINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, QUERY_DEFINE, VALUE);
    r = r && query_2(b, l + 1);
    r = r && consumeToken(b, QUERY_SEPARATOR);
    r = r && queryStatement(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, QUERY, r);
    return r;
  }

  // inputArguments?
  private static boolean query_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_2")) return false;
    inputArguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // inputArgument | queryPath
  public static boolean queryPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PART, "<query part>");
    r = inputArgument(b, l + 1);
    if (!r) r = queryPath(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // KEY COLON KEY
  public static boolean queryPath(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryPath")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, COLON, KEY);
    exit_section_(b, m, QUERY_PATH, r);
    return r;
  }

  /* ********************************************************** */
  // (SLASH queryPart | queryPart)+
  public static boolean queryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_STATEMENT, "<query statement>");
    r = queryStatement_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!queryStatement_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "queryStatement", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SLASH queryPart | queryPart
  private static boolean queryStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = queryStatement_0_0(b, l + 1);
    if (!r) r = queryPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SLASH queryPart
  private static boolean queryStatement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SLASH);
    r = r && queryPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (variableDeclare | variableAssignment)*
  public static boolean scriptBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scriptBlock")) return false;
    Marker m = enter_section_(b, l, _NONE_, SCRIPT_BLOCK, "<script block>");
    while (true) {
      int c = current_position_(b);
      if (!scriptBlock_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scriptBlock", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // variableDeclare | variableAssignment
  private static boolean scriptBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scriptBlock_0")) return false;
    boolean r;
    r = variableDeclare(b, l + 1);
    if (!r) r = variableAssignment(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // DOLLAR KEY variableType? variableValue?
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, KEY);
    r = r && variable_2(b, l + 1);
    r = r && variable_3(b, l + 1);
    exit_section_(b, m, VARIABLE, r);
    return r;
  }

  // variableType?
  private static boolean variable_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_2")) return false;
    variableType(b, l + 1);
    return true;
  }

  // variableValue?
  private static boolean variable_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_3")) return false;
    variableValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (DOLLAR KEY EQUALS VALUE) | (DOLLAR VALUE EQUALS VALUE)
  public static boolean variableAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignment")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableAssignment_0(b, l + 1);
    if (!r) r = variableAssignment_1(b, l + 1);
    exit_section_(b, m, VARIABLE_ASSIGNMENT, r);
    return r;
  }

  // DOLLAR KEY EQUALS VALUE
  private static boolean variableAssignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignment_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, KEY, EQUALS, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  // DOLLAR VALUE EQUALS VALUE
  private static boolean variableAssignment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignment_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, VALUE, EQUALS, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (DECLARE_VAR DOLLAR KEY variableValue?) | (DECLARE_VAR DOLLAR VALUE variableValue?)
  public static boolean variableDeclare(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclare")) return false;
    if (!nextTokenIs(b, DECLARE_VAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableDeclare_0(b, l + 1);
    if (!r) r = variableDeclare_1(b, l + 1);
    exit_section_(b, m, VARIABLE_DECLARE, r);
    return r;
  }

  // DECLARE_VAR DOLLAR KEY variableValue?
  private static boolean variableDeclare_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclare_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DECLARE_VAR, DOLLAR, KEY);
    r = r && variableDeclare_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // variableValue?
  private static boolean variableDeclare_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclare_0_3")) return false;
    variableValue(b, l + 1);
    return true;
  }

  // DECLARE_VAR DOLLAR VALUE variableValue?
  private static boolean variableDeclare_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclare_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DECLARE_VAR, DOLLAR, VALUE);
    r = r && variableDeclare_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // variableValue?
  private static boolean variableDeclare_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclare_1_3")) return false;
    variableValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // PARENTHESIS_OPEN queryPath PARENTHESIS_CLOSED
  public static boolean variableType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableType")) return false;
    if (!nextTokenIs(b, PARENTHESIS_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PARENTHESIS_OPEN);
    r = r && queryPath(b, l + 1);
    r = r && consumeToken(b, PARENTHESIS_CLOSED);
    exit_section_(b, m, VARIABLE_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // EQUALS VALUE
  public static boolean variableValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableValue")) return false;
    if (!nextTokenIs(b, EQUALS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, EQUALS, VALUE);
    exit_section_(b, m, VARIABLE_VALUE, r);
    return r;
  }

}
