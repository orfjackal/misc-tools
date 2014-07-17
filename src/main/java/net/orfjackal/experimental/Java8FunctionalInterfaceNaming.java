package net.orfjackal.experimental;

import com.google.common.primitives.Primitives;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Esko Luontola
 * @since 17.7.2014
 */
public class Java8FunctionalInterfaceNaming {

    private final String[] args;
    private final String returns;

    public Java8FunctionalInterfaceNaming(String args, String returns) {
        this.returns = returns;
        this.args = Arrays.asList(args.split(", "))
                .stream()
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public String getClassName() {

        // Suppliers
        if (args.length == 0) {
            return tagPrimitive(returns) + "Supplier";
        }

        // Consumers
        if (returns(void.class)) {
            if (args.length == 1) {
                return tagPrimitives(args) + "Consumer";
            }
            if (args.length == 2) {
                if (isGeneric(args[0]) && isGeneric(args[1])) {
                    return "BiConsumer";
                }
                return tagObjOrPrimitives(args) + "Consumer";
            }
        }

        // Predicates
        if (returns(boolean.class)) {
            if (args.length == 1) {
                return tagPrimitives(args) + "Predicate";
            }
            if (args.length == 2) {
                return tagPrimitives(args) + "BiPredicate";
            }
        }

        // Operators
        if (allEqual(args, returns)) {
            if (args.length == 1) {
                return tagPrimitive(returns) + "UnaryOperator";
            }
            if (args.length == 2) {
                return tagPrimitive(returns) + "BinaryOperator";
            }
        }

        // Functions
        if (args.length == 1) {
            return tagPrimitives(args) + tagToPrimitive(returns) + "Function";
        }
        if (args.length == 2) {
            return tagPrimitives(args) + tagToPrimitive(returns) + "BiFunction";
        }
        return null;
    }

    public String getMethodName() {

        // Suppliers
        if (args.length == 0) {
            return "get" + tagAsPrimitive(returns);
        }

        // Consumers
        if (returns(void.class)) {
            return "accept";
        }

        // Predicates
        if (returns(boolean.class)) {
            return "test";
        }

        // Functions & Operators
        return "apply" + tagAsPrimitive(returns);
    }

    private static String tagObjOrPrimitives(String... types) {
        return join(Java8FunctionalInterfaceNaming::tagObjOrPrimitive, types);
    }

    private static String tagPrimitives(String... types) {
        return join(Java8FunctionalInterfaceNaming::tagPrimitive, types);
    }

    private static String join(Function<String, String> fn, String... types) {
        return Arrays.asList(types).stream()
                .map(fn)
                .collect(Collectors.joining());
    }

    private static String tagObjOrPrimitive(String type) {
        if (isPrimitive(type)) {
            return capitalize(type);
        } else {
            return "Obj";
        }
    }

    private static String tagPrimitive(String type) {
        if (isPrimitive(type)) {
            return capitalize(type);
        } else {
            return "";
        }
    }

    private static String tagAsPrimitive(String type) {
        if (isPrimitive(type)) {
            return "As" + capitalize(type);
        } else {
            return "";
        }
    }

    private static String tagToPrimitive(String type) {
        if (isPrimitive(type)) {
            return "To" + capitalize(type);
        } else {
            return "";
        }
    }

    private static boolean isPrimitive(String type) {
        return !isGeneric(type);
    }

    private static boolean isGeneric(String type) {
        return type.length() == 1;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private boolean returns(Class<?> type) {
        if (type == void.class) {
            return returns.equals("");
        } else {
            return returns.equals(type.getName());
        }
    }

    private static boolean allEqual(String[] args, String returns) {
        for (String arg : args) {
            if (!arg.equals(returns)) {
                return false;
            }
        }
        return true;
    }

    public Class<?>[] getParameterTypes() {
        return Arrays.asList(args).stream()
                .map(Java8FunctionalInterfaceNaming::stringToClass)
                .toArray(Class<?>[]::new);
    }

    public Class<?> getReturnType() {
        return stringToClass(returns);
    }

    private static Class<?> stringToClass(String type) {
        if (type.equals("")) {
            return void.class;
        }
        if (isGeneric(type)) {
            return Object.class;
        }
        return Primitives.allPrimitiveTypes().stream()
                .filter(c -> c.getName().equals(type))
                .findAny()
                .get();
    }
}