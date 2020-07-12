// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.impl.*;

public interface OMTTypes {

  IElementType INPUT_ARGUMENT = new OMTElementType("INPUT_ARGUMENT");
  IElementType INPUT_ARGUMENTS = new OMTElementType("INPUT_ARGUMENTS");
  IElementType LIST_ITEM = new OMTElementType("LIST_ITEM");
  IElementType MODEL = new OMTElementType("MODEL");
  IElementType MODEL_BLOCK_CONTENT = new OMTElementType("MODEL_BLOCK_CONTENT");
  IElementType MODEL_BLOCK_GROUP = new OMTElementType("MODEL_BLOCK_GROUP");
  IElementType MODEL_BLOCK_ID = new OMTElementType("MODEL_BLOCK_ID");
  IElementType MODEL_ITEM = new OMTElementType("MODEL_ITEM");
  IElementType PREFIX = new OMTElementType("PREFIX");
  IElementType PROPERTY = new OMTElementType("PROPERTY");
  IElementType QUERY = new OMTElementType("QUERY");
  IElementType QUERY_PART = new OMTElementType("QUERY_PART");
  IElementType QUERY_PATH = new OMTElementType("QUERY_PATH");
  IElementType QUERY_STATEMENT = new OMTElementType("QUERY_STATEMENT");
  IElementType SCRIPT_BLOCK = new OMTElementType("SCRIPT_BLOCK");
  IElementType VARIABLE = new OMTElementType("VARIABLE");
  IElementType VARIABLE_ASSIGNMENT = new OMTElementType("VARIABLE_ASSIGNMENT");
  IElementType VARIABLE_DECLARE = new OMTElementType("VARIABLE_DECLARE");
  IElementType VARIABLE_TYPE = new OMTElementType("VARIABLE_TYPE");
  IElementType VARIABLE_VALUE = new OMTElementType("VARIABLE_VALUE");

  IElementType COLON = new OMTTokenType("COLON");
  IElementType COMMA = new OMTTokenType("COMMA");
  IElementType COMMENT = new OMTTokenType("COMMENT");
  IElementType CRLF = new OMTTokenType("CRLF");
  IElementType DECLARE_VAR = new OMTTokenType("DECLARE_VAR");
  IElementType DOLLAR = new OMTTokenType("DOLLAR");
  IElementType EQUALS = new OMTTokenType("EQUALS");
  IElementType KEY = new OMTTokenType("KEY");
  IElementType LISTITEM_BULLET = new OMTTokenType("LISTITEM_BULLET");
  IElementType MODEL_ITEM_TYPE = new OMTTokenType("MODEL_ITEM_TYPE");
  IElementType PARENTHESIS_CLOSED = new OMTTokenType("PARENTHESIS_CLOSED");
  IElementType PARENTHESIS_OPEN = new OMTTokenType("PARENTHESIS_OPEN");
  IElementType PIPE = new OMTTokenType("PIPE");
  IElementType PREFIX_IRI = new OMTTokenType("PREFIX_IRI");
  IElementType QUERY_DEFINE = new OMTTokenType("QUERY_DEFINE");
  IElementType QUERY_SEPARATOR = new OMTTokenType("QUERY_SEPARATOR");
  IElementType SEMICOLON = new OMTTokenType("SEMICOLON");
  IElementType SLASH = new OMTTokenType("SLASH");
  IElementType VALUE = new OMTTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == INPUT_ARGUMENT) {
        return new OMTInputArgumentImpl(node);
      }
      else if (type == INPUT_ARGUMENTS) {
        return new OMTInputArgumentsImpl(node);
      }
      else if (type == LIST_ITEM) {
        return new OMTListItemImpl(node);
      }
      else if (type == MODEL) {
        return new OMTModelImpl(node);
      }
      else if (type == MODEL_BLOCK_CONTENT) {
        return new OMTModelBlockContentImpl(node);
      }
      else if (type == MODEL_BLOCK_GROUP) {
        return new OMTModelBlockGroupImpl(node);
      }
      else if (type == MODEL_BLOCK_ID) {
        return new OMTModelBlockIdImpl(node);
      }
      else if (type == MODEL_ITEM) {
        return new OMTModelItemImpl(node);
      }
      else if (type == PREFIX) {
        return new OMTPrefixImpl(node);
      }
      else if (type == PROPERTY) {
        return new OMTPropertyImpl(node);
      }
      else if (type == QUERY) {
        return new OMTQueryImpl(node);
      }
      else if (type == QUERY_PART) {
        return new OMTQueryPartImpl(node);
      }
      else if (type == QUERY_PATH) {
        return new OMTQueryPathImpl(node);
      }
      else if (type == QUERY_STATEMENT) {
        return new OMTQueryStatementImpl(node);
      }
      else if (type == SCRIPT_BLOCK) {
        return new OMTScriptBlockImpl(node);
      }
      else if (type == VARIABLE) {
        return new OMTVariableImpl(node);
      }
      else if (type == VARIABLE_ASSIGNMENT) {
        return new OMTVariableAssignmentImpl(node);
      }
      else if (type == VARIABLE_DECLARE) {
        return new OMTVariableDeclareImpl(node);
      }
      else if (type == VARIABLE_TYPE) {
        return new OMTVariableTypeImpl(node);
      }
      else if (type == VARIABLE_VALUE) {
        return new OMTVariableValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
