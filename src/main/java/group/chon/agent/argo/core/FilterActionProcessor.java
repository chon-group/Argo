package group.chon.agent.argo.core;

import group.chon.agent.argo.core.utils.ArgsUtils;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterActionProcessor {

    public static String[] doFilters(String[] perceptions, List<Term[]> filtersConfigurations) throws ParseException, ScriptException {
        List<String> perceptionsList = new ArrayList<>();
        for (int i = 0; i < filtersConfigurations.size(); i++) {
            Term[] filtersConfiguration = filtersConfigurations.get(i);
            String filterActionName = ArgsUtils.getInString(filtersConfiguration[0]);
            FilterActionEnum filterAction = FilterActionEnum.getFilterAction(filterActionName);
            if (filterAction != null) {
                if (filtersConfiguration[1].isList()) {
                    ListTerm filterConfigurationListTerm = ASSyntax.parseList(ArgsUtils.getInString(filtersConfiguration[1]));
                    if (FilterActionEnum.EXCEPT.equals(filterAction)) {
                        for (Term filterConfigurationTerm : filterConfigurationListTerm) {
                            perceptionsList = choiceFilterToBeDone(filterConfigurationTerm,
                                    filtersConfiguration,
                                    filterAction,
                                    perceptionsList,
                                    perceptions,
                                    0);
                        }
                    } else {
                        for (int j = 0; j < filterConfigurationListTerm.size(); j++) {
                            perceptionsList = choiceFilterToBeDone(filterConfigurationListTerm.get(j),
                                    filtersConfiguration,
                                    filterAction,
                                    perceptionsList,
                                    perceptions,
                                    j);
                        }
                    }
                } else {
                    perceptionsList = choiceFilterToBeDone(filtersConfiguration[1],
                            filtersConfiguration,
                            filterAction,
                            perceptionsList,
                            perceptions,
                            i);
                }
            }
        }

        return perceptionsList.toArray(new String[0]);
    }

    private static List<String> choiceFilterToBeDone(Term filtersConfiguration,
                                                     Term[] filtersConfigurationList,
                                                     FilterActionEnum filterAction,
                                                     List<String> perceptionsList,
                                                     String[] perceptions,
                                                     int i) throws ParseException, ScriptException {
        Literal perceptionToBeFiltered = ASSyntax.parseLiteral(ArgsUtils.getInString(filtersConfiguration));

        if (FilterActionEnum.EXCEPT.equals(filterAction)) {
            perceptionsList = doExceptFilter(perceptions, filtersConfigurationList, perceptionToBeFiltered, perceptionsList, i == 0);
        } else if (FilterActionEnum.ONLY.equals(filterAction)) {
            perceptionsList = doOnlyFilter(perceptions, filtersConfigurationList, perceptionToBeFiltered, perceptionsList, i == 0);
        } else if (FilterActionEnum.VALUE.equals(filterAction)) {
            perceptionsList = doValueFilter(perceptions, filtersConfigurationList, perceptionToBeFiltered, perceptionsList, i == 0);
        } else {
            // Erro.

        }
        return perceptionsList;
    }

    public static List<String> getVariables(String condition) {
        String regex = "\\b[A-Z][a-zA-Z]*\\b";

        // Compilar a expressão regular
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(condition);

        // Lista para armazenar as palavras encontradas
        List<String> variables = new ArrayList<>();

        // Encontrar todas as correspondências
        while (matcher.find()) {
            String newWord = matcher.group();
            if (!variables.contains(newWord)) {
                variables.add(newWord);
            }
        }
        return variables;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static String rewritingCondition(String condition, List<String> variales, Term term) {
        String newCondition = condition;

        for (String variable : variales) {
            newCondition = newCondition.replace(variable, term.toString());
        }

        String regex2 = "\\b\\w+\\b";

        // Compilar a expressão regular
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(newCondition);

        // StringBuilder para armazenar a nova string
        StringBuffer resultBuffer = new StringBuffer();

        // Encontrar todas as correspondências e substituí-las por versões entre aspas
        while (matcher2.find()) {
            String group = matcher2.group();
            if (isInteger(group) || isDouble(group)) {
                matcher2.appendReplacement(resultBuffer, group);
            } else {
                matcher2.appendReplacement(resultBuffer, "\"" + group + "\"");
            }
        }
        matcher2.appendTail(resultBuffer);

        newCondition = resultBuffer.toString();

        return newCondition;
    }


    public static boolean comparator(LogicalFormula formula, List<Term> termList) throws ScriptException {
        String condition = formula.toString();
        condition = condition.replace("=", "==");
        condition = condition.replace("|", "||");
        condition = condition.replace("&", "&&");

        List<String> variales = getVariables(condition);

        for (Term term : termList) {
            String newCondition = rewritingCondition(condition, variales, term);

            Context context = Context.create();
            Value result = context.eval("js", newCondition);

            // Avaliar a expressão
            //Boolean result = (Boolean) engine.eval(newCondition);
            if (result.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static List<String> doExceptFilter(String[] perceptions,
                                              Term[] filtersConfiguration,
                                              Literal perceptionToBeFiltered,
                                              List<String> perceptionsList,
                                              boolean isFirstFilter) throws ParseException, ScriptException {
        String[] perceptionsForFilter = perceptions;
        if (!isFirstFilter) {
            perceptionsForFilter = perceptionsList.toArray(new String[0]);
            perceptionsList = new ArrayList<>();
        }
        for (int i = 0; i < perceptionsForFilter.length; i++) {
            Literal perceptionLiteral = ASSyntax.parseLiteral(perceptionsForFilter[i]);
            if (perceptionToBeFiltered.getFunctor().equalsIgnoreCase(perceptionLiteral.getFunctor())) {
                if(filtersConfiguration.length >= 3) {
                    LogicalFormula condition = ASSyntax.parseFormula(filtersConfiguration[2].toString());
                    if(comparator(condition, perceptionLiteral.getTerms())) {
                        perceptionsList.add(perceptionsForFilter[i]);
                    }
                } else {
                    if(perceptionToBeFiltered.getTerms() != null && !perceptionToBeFiltered.getTerms().isEmpty()
                            && perceptionLiteral.getTerms() != null && !perceptionLiteral.getTerms().isEmpty()) {
                        if (new HashSet<>(perceptionLiteral.getTerms()).containsAll(perceptionToBeFiltered.getTerms())) {
                            perceptionsList.add(perceptionsForFilter[i]);
                        } else {
                            if(perceptionToBeFiltered.getTerms().stream().allMatch(Term::isVar)) {
                                perceptionsList.add(perceptionsForFilter[i]);
                            }
                        }
                    } else {
                        perceptionsList.add(perceptionsForFilter[i]);
                    }
                }
            }
        }
        return perceptionsList;
    }

    public static List<String> doOnlyFilter(String[] perceptions,
                                            Term[] filtersConfiguration,
                                            Literal perceptionToBeFiltered,
                                            List<String> perceptionsList,
                                            boolean isFirstFilter) throws ParseException, ScriptException {
        String[] perceptionsForFilter = perceptions;
        if (!isFirstFilter) {
            perceptionsForFilter = perceptionsList.toArray(new String[0]);
            perceptionsList = new ArrayList<>();
        }
        for (int i = 0; i < perceptionsForFilter.length; i++) {
            Literal perceptionLiteral = ASSyntax.parseLiteral(perceptionsForFilter[i]);
            if (perceptionToBeFiltered.getFunctor().equalsIgnoreCase(perceptionLiteral.getFunctor())) {
                if(filtersConfiguration.length >= 3) {
                    LogicalFormula condition = ASSyntax.parseFormula(filtersConfiguration[2].toString());
                    if(!comparator(condition, perceptionLiteral.getTerms())) {
                        perceptionsList.add(perceptionsForFilter[i]);
                    }
                } else {
                    if(perceptionToBeFiltered.getTerms() != null && !perceptionToBeFiltered.getTerms().isEmpty()
                            && perceptionLiteral.getTerms() != null && !perceptionLiteral.getTerms().isEmpty()) {
                        if (!new HashSet<>(perceptionLiteral.getTerms()).containsAll(perceptionToBeFiltered.getTerms())
                                && !perceptionToBeFiltered.getTerms().stream().allMatch(Term::isVar)) {
                            perceptionsList.add(perceptionsForFilter[i]);
                        }
                    }
                }
            } else {
                perceptionsList.add(perceptionsForFilter[i]);
            }
        }
        return perceptionsList;
    }

    public static List<String> doValueFilter(String[] perceptions,
                                             Term[] filtersConfiguration,
                                             Literal perceptionToBeFiltered,
                                             List<String> perceptionsList,
                                             boolean isFirstFilter) throws ParseException, ScriptException {
        String[] perceptionsForFilter = perceptions;
        if (!isFirstFilter) {
            perceptionsForFilter = perceptionsList.toArray(new String[0]);
            perceptionsList = new ArrayList<>();
        }
        for (int i = 0; i < perceptionsForFilter.length; i++) {
            Literal perceptionLiteral = ASSyntax.parseLiteral(perceptionsForFilter[i]);
            if (perceptionToBeFiltered.getFunctor().equalsIgnoreCase(perceptionLiteral.getFunctor())) {
                if(filtersConfiguration.length >= 3) {
                    LogicalFormula condition = ASSyntax.parseFormula(filtersConfiguration[2].toString());
                    if(!comparator(condition, perceptionLiteral.getTerms())) {
                        perceptionsList.add(perceptionsForFilter[i]);
                    }
                } else {
                    if(perceptionToBeFiltered.getTerms() != null && !perceptionToBeFiltered.getTerms().isEmpty()
                            && perceptionLiteral.getTerms() != null && !perceptionLiteral.getTerms().isEmpty()) {
                        if (!new HashSet<>(perceptionLiteral.getTerms()).containsAll(perceptionToBeFiltered.getTerms())
                                && !perceptionToBeFiltered.getTerms().stream().allMatch(Term::isVar)) {
                            perceptionsList.add(perceptionsForFilter[i]);
                        }
                    }
                }
            } else {
                perceptionsList.add(perceptionsForFilter[i]);
            }
        }
        return perceptionsList;
    }
}
