//package com.misset.opp.omt.psi.util;
//
//import com.misset.opp.omt.psi.*;
//
//import java.util.List;
//import java.util.Optional;
//
//public class OMTQueryUtil {
//
//    public static boolean isPrefixDefined(OMTQueryPath path) {
//        List<OMTPrefix> prefixes = OMTUtil.getPrefixes(path);
//        String pathPrefix = getPrefixName(path);
//        for(OMTPrefix prefix : prefixes) {
//            String prefixName = getPrefixName(prefix);
//            if(prefixName.equals(pathPrefix)) { return true; }
//        }
//        return false;
//    }
//    public static boolean isQueryDefined(OMTQueryOperator queryOperator) {
//        Optional<OMTQuery> definedByRoot = getDefinedByRoot(queryOperator);
//        if(definedByRoot.isPresent()) {
//            System.out.println(queryOperator.getText() + " is defined by " + definedByRoot.get().getText());
//        }
//
//        return true;
//    }
//    public static Optional<OMTQuery> getDefinedByRoot(OMTQueryOperator queryOperator) {
//        List<OMTQuery> rootQueries = OMTUtil.getRootQueries(queryOperator);
//        for(OMTQuery rootQuery : rootQueries) {
//            if(rootQuery.getChildren()[1].getText().equals(queryOperator.getFirstChild().getText())) {
//                return Optional.of(rootQuery);
//            }
//        }
//        return Optional.empty();
//    }
//
//    public static String getPrefixName(OMTQueryPath path) {
//        return path.getFirstChild().getText();
//    }
//    public static String getPrefixName(OMTPrefix prefix) {
//        return prefix.getFirstChild().getText();
//    }
//}
