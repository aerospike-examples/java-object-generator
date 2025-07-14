package com.aerospike.generator.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ExpressionParser {
    // Node interface for the AST
    public interface Node {
        <T> T accept(Visitor<T> visitor);
    }

    // Visitor interface for evaluating expressions
    public interface Visitor<T> {
        T visitNumber(long value);
        T visitString(String value);
        T visitBinary(BinaryOp op, Node left, Node right);
        T visitParameter(String name);
        T visitFunction(String name, Node[] args);
    }

    // Binary operation types
    public enum BinaryOp {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER, CONCAT
    }

    // Token types for parsing
    private enum TokenType {
        NUMBER, STRING, PARAMETER, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER, CONCAT,
        LEFT_PAREN, RIGHT_PAREN, COMMA, IDENTIFIER, EOF
    }

    private static class Token {
        final TokenType type;
        final String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    // Concrete node classes
    public static class NumberNode implements Node {
        private final long value;

        public NumberNode(long value) {
            this.value = value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitNumber(value);
        }
    }

    public static class StringNode implements Node {
        private final String value;

        public StringNode(String value) {
            this.value = value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitString(value);
        }
    }

    public static class BinaryNode implements Node {
        private final BinaryOp op;
        private final Node left;
        private final Node right;

        public BinaryNode(BinaryOp op, Node left, Node right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinary(op, left, right);
        }
    }

    public static class ParameterNode implements Node {
        private final String name;

        public ParameterNode(String name) {
            this.name = name;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitParameter(name);
        }
    }

    public static class FunctionNode implements Node {
        private final String name;
        private final Node[] arguments;

        public FunctionNode(String name, Node[] arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitFunction(name, arguments);
        }
    }

    // Expression evaluator
    public static class ExpressionEvaluator implements Visitor<Object> {
        private final Map<String, Object> parameters;
        private final boolean returnString;

        public ExpressionEvaluator(Map<String, Object> parameters, boolean returnString) {
            this.parameters = parameters;
            this.returnString = returnString;
        }

        private long toLong(Object value) {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof String) {
                try {
                    return Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot convert string to number: " + value);
                }
            } else {
                throw new IllegalArgumentException("Cannot convert to number: " + value);
            }
        }
        
        @Override
        public Object visitNumber(long value) {
            return returnString ? String.valueOf(value) : value;
        }

        @Override
        public Object visitString(String value) {
            return value;
        }

        @Override
        public Object visitBinary(BinaryOp op, Node left, Node right) {
            Object leftValue = left.accept(this);
            Object rightValue = right.accept(this);

            if (op == BinaryOp.CONCAT) {
                return String.valueOf(leftValue) + String.valueOf(rightValue);
            }

            // Convert to numbers for arithmetic operations
            long leftNum = toLong(leftValue);
            long rightNum = toLong(rightValue);

            switch (op) {
                case ADD: return returnString ? String.valueOf(leftNum + rightNum) : leftNum + rightNum;
                case SUBTRACT: return returnString ? String.valueOf(leftNum - rightNum) : leftNum - rightNum;
                case MULTIPLY: return returnString ? String.valueOf(leftNum * rightNum) : leftNum * rightNum;
                case DIVIDE: return returnString ? String.valueOf(leftNum / rightNum) : leftNum / rightNum;
                case MODULO: return returnString ? String.valueOf(leftNum % rightNum) : leftNum % rightNum;
                case POWER: return returnString ? String.valueOf((long) Math.pow(leftNum, rightNum)) : (long) Math.pow(leftNum, rightNum);
                default: throw new IllegalStateException("Unknown operator: " + op);
            }
        }

        @Override
        public Object visitParameter(String name) {
            Object value = parameters.get(name);
            if (value == null) {
                throw new IllegalArgumentException("Parameter not found: " + name);
            }
            return returnString ? String.valueOf(value) : value;
        }

        @Override
        public Object visitFunction(String name, Node[] args) {
            switch (name.toUpperCase()) {
                case "NOW":
                    if (args.length != 0) {
                        throw new IllegalArgumentException("NOW() takes no arguments");
                    }
                    long now = System.currentTimeMillis();
                    return returnString ? String.valueOf(now) : now;

                case "DATE":
                    if (args.length < 1 || args.length > 2) {
                        throw new IllegalArgumentException("DATE() takes 1 or 2 arguments");
                    }
                    Object timestamp = args[0].accept(this);
                    long time = toLong(timestamp);
                    
                    String format = "yyyy-MM-dd";
                    if (args.length == 2) {
                        Object formatObj = args[1].accept(this);
                        format = String.valueOf(formatObj);
                    }
                    
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(time), ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    return dateTime.format(formatter);

                case "PAD":
                    if (args.length != 3) {
                        throw new IllegalArgumentException("PAD() takes exactly 3 arguments");
                    }
                    Object value = args[0].accept(this);
                    Object lengthObj = args[1].accept(this);
                    Object padCharObj = args[2].accept(this);
                    
                    String strValue = String.valueOf(value);
                    int length = Integer.parseInt(String.valueOf(lengthObj));
                    String padChar = String.valueOf(padCharObj);
                    
                    if (padChar.length() != 1) {
                        throw new IllegalArgumentException("PAD() third argument must be a single character");
                    }
                    
                    if (strValue.length() >= length) {
                        return strValue;
                    }
                    
                    StringBuilder padded = new StringBuilder();
                    for (int i = strValue.length(); i < length; i++) {
                        padded.append(padChar);
                    }
                    padded.append(strValue);
                    return padded.toString();

                case "UUID":
                    if (args.length == 0) {
                        return UUID.randomUUID().toString();
                    }
                    else if (args.length == 1) {
                        Object modifier = args[0].accept(this);
                        long longVal = toLong(modifier);
                        String baseUuid = String.format("01234567-890a-bcde-f012-%012d", longVal);
                        return baseUuid;
                    }
                    else {
                        throw new IllegalArgumentException("UUID() takes 0 or 1 arguments");
                    }
                    
                default:
                    throw new IllegalArgumentException("Unknown function: " + name);
            }
        }
    }

    // Parser implementation
    private static class Parser {
        private final String input;
        private int pos = 0;
        private Token currentToken;

        public Parser(String input) {
            this.input = input;
            this.currentToken = null;
        }

        private void advance() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }

            if (pos >= input.length()) {
                currentToken = new Token(TokenType.EOF, "");
                return;
            }

            char c = input.charAt(pos);
            switch (c) {
                case '+': pos++; currentToken = new Token(TokenType.PLUS, "+"); break;
                case '-': pos++; currentToken = new Token(TokenType.MINUS, "-"); break;
                case '*': pos++; currentToken = new Token(TokenType.MULTIPLY, "*"); break;
                case '/': pos++; currentToken = new Token(TokenType.DIVIDE, "/"); break;
                case '%': pos++; currentToken = new Token(TokenType.MODULO, "%"); break;
                case '^': pos++; currentToken = new Token(TokenType.POWER, "^"); break;
                case '&': pos++; currentToken = new Token(TokenType.CONCAT, "&"); break;
                case '(': pos++; currentToken = new Token(TokenType.LEFT_PAREN, "("); break;
                case ')': pos++; currentToken = new Token(TokenType.RIGHT_PAREN, ")"); break;
                case ',': pos++; currentToken = new Token(TokenType.COMMA, ","); break;
                case '\'':
                    pos++;
                    StringBuilder str = new StringBuilder();
                    while (pos < input.length() && input.charAt(pos) != '\'') {
                        str.append(input.charAt(pos++));
                    }
                    if (pos >= input.length() || input.charAt(pos) != '\'') {
                        throw new IllegalArgumentException("Expected '\''");
                    }
                    pos++;
                    currentToken = new Token(TokenType.STRING, str.toString());
                    break;
                case '$':
                    pos++;
                    StringBuilder param = new StringBuilder();
                    while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                        param.append(input.charAt(pos++));
                    }
                    currentToken = new Token(TokenType.PARAMETER, param.toString());
                    break;
                default:
                    if (Character.isDigit(c)) {
                        StringBuilder num = new StringBuilder();
                        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                            num.append(input.charAt(pos++));
                        }
                        currentToken = new Token(TokenType.NUMBER, num.toString());
                    } else if (Character.isLetter(c)) {
                        StringBuilder id = new StringBuilder();
                        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
                            id.append(input.charAt(pos++));
                        }
                        currentToken = new Token(TokenType.IDENTIFIER, id.toString());
                    } else {
                        throw new IllegalArgumentException("Unexpected character: " + c);
                    }
            }
        }

        public Node parse() {
            advance();
            return parseExpression();
        }

        private Node parseExpression() {
            Node left = parseTerm();
            while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS || 
                   currentToken.type == TokenType.CONCAT) {
                Token op = currentToken;
                advance();
                Node right = parseTerm();
                BinaryOp binaryOp = op.type == TokenType.PLUS ? BinaryOp.ADD :
                                  op.type == TokenType.MINUS ? BinaryOp.SUBTRACT :
                                  BinaryOp.CONCAT;
                left = new BinaryNode(binaryOp, left, right);
            }
            return left;
        }

        private Node parseTerm() {
            Node left = parseFactor();
            while (currentToken.type == TokenType.MULTIPLY || currentToken.type == TokenType.DIVIDE || 
                   currentToken.type == TokenType.MODULO) {
                Token op = currentToken;
                advance();
                Node right = parseFactor();
                BinaryOp binaryOp = op.type == TokenType.MULTIPLY ? BinaryOp.MULTIPLY :
                                  op.type == TokenType.DIVIDE ? BinaryOp.DIVIDE :
                                  BinaryOp.MODULO;
                left = new BinaryNode(binaryOp, left, right);
            }
            return left;
        }

        private Node parseFactor() {
            Node left = parsePrimary();
            while (currentToken.type == TokenType.POWER) {
                advance();
                Node right = parsePrimary();
                left = new BinaryNode(BinaryOp.POWER, left, right);
            }
            return left;
        }

        private Node parsePrimary() {
            Token token = currentToken;
            advance();

            switch (token.type) {
                case NUMBER:
                    return new NumberNode(Long.parseLong(token.value));
                case STRING:
                    return new StringNode(token.value);
                case PARAMETER:
                    return new ParameterNode(token.value);
                case IDENTIFIER:
                    if (currentToken.type != TokenType.LEFT_PAREN) {
                        throw new IllegalArgumentException("Expected '(' after function name");
                    }
                    advance();
                    Node[] args = parseArguments();
                    return new FunctionNode(token.value, args);
                case LEFT_PAREN:
                    Node expr = parseExpression();
                    if (currentToken.type != TokenType.RIGHT_PAREN) {
                        throw new IllegalArgumentException("Expected ')'");
                    }
                    advance();
                    return expr;
                default:
                    throw new IllegalArgumentException("Unexpected token: " + token.type);
            }
        }

        private Node[] parseArguments() {
            if (currentToken.type == TokenType.RIGHT_PAREN) {
                advance();
                return new Node[0];
            }

            java.util.List<Node> args = new java.util.ArrayList<>();
            args.add(parseExpression());

            while (currentToken.type == TokenType.COMMA) {
                advance();
                args.add(parseExpression());
            }

            if (currentToken.type != TokenType.RIGHT_PAREN) {
                throw new IllegalArgumentException("Expected ')'");
            }
            advance();

            return args.toArray(new Node[0]);
        }
    }

    /**
     * Evaluates a mathematical and string expression with support for parameters and functions.
     * 
     * <p>The expression can contain the following elements:</p>
     * 
     * <h3>Numbers and Strings</h3>
     * <ul>
     *   <li>Integer numbers: {@code 42, 123, 0}</li>
     *   <li>String literals (using single quotes): {@code 'Hello', 'World'}</li>
     * </ul>
     * 
     * <h3>Arithmetic Operators</h3>
     * <ul>
     *   <li>Addition: {@code +}</li>
     *   <li>Subtraction: {@code -}</li>
     *   <li>Multiplication: {@code *}</li>
     *   <li>Division: {@code /}</li>
     *   <li>Modulo: {@code %}</li>
     *   <li>Power: {@code ^}</li>
     * </ul>
     * 
     * <h3>String Operations</h3>
     * <ul>
     *   <li>String concatenation: {@code &}</li>
     * </ul>
     * 
     * <h3>Parameters</h3>
     * <ul>
     *   <li>Parameter reference: {@code $key}</li>
     *   <li>Parameters must be provided in the parameters map</li>
     *   <li>Parameters are of type Long</li>
     * </ul>
     * 
     * <h3>Functions</h3>
     * <ul>
     *   <li>{@code NOW()} - Returns current timestamp in milliseconds</li>
     *   <li>{@code DATE(timestamp, [format])} - Formats a timestamp as a string
     *       <ul>
     *         <li>First parameter: timestamp (long)</li>
     *         <li>Second parameter (optional): date format string (defaults to 'yyyy-MM-dd')</li>
     *       </ul>
     *   </li>
     *   <li>{@code PAD(value, length, padChar)} - Left-pads a value to a specified length
     *       <ul>
     *         <li>First parameter: value to pad (string or number)</li>
     *         <li>Second parameter: desired length (integer)</li>
     *         <li>Third parameter: character to use for padding (single character)</li>
     *       </ul>
     *   </li>
     * </ul>
     * 
     * <h3>Operator Precedence</h3>
     * <ol>
     *   <li>Parentheses {@code ()}</li>
     *   <li>Power {@code ^}</li>
     *   <li>Multiplication/Division/Modulo {@code * / %}</li>
     *   <li>Addition/Subtraction {@code + -}</li>
     *   <li>String concatenation {@code &}</li>
     * </ol>
     * 
     * <h3>Examples</h3>
     * <pre>
     * "2 + 3 * 4"                    // Evaluates to 14
     * "$key * 2 + 5"                 // Uses parameter value
     * "'Hello' & ' World'"           // String concatenation
     * "($key + 10) * 2"             // Parentheses for grouping
     * "NOW()"                        // Current timestamp
     * "DATE(NOW())"                  // Current date in yyyy-MM-dd format
     * "DATE(NOW(), 'HH:mm:ss')"      // Current time in custom format
     * "PAD(42, 5, '0')"             // Returns "00042"
     * "PAD('ABC', 5, '*')"          // Returns "**ABC"
     * </pre>
     * 
     * @param expression The expression to evaluate
     * @param parameters Map of parameter names to their values
     * @param returnString If true, numeric results are converted to strings
     * @return The result of the expression evaluation. If returnString is true, the result will be a String,
     *         otherwise it will be a Long for numeric operations or String for string operations
     * @throws IllegalArgumentException if the expression is invalid or contains unknown functions
     * @throws ArithmeticException if a division by zero occurs
     */
    public Object evaluate(String expression, Map<String, Object> parameters, boolean returnString) {
        Node ast = parseExpression(expression);
        return evaluate(ast, parameters, returnString);
    }
    
    public Node parseExpression(String expression) {
        Parser parser = new Parser(expression);
        return parser.parse();
    }
    
    public Object evaluate(Node ast, Map<String, Object> parameters, boolean returnString) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(parameters, returnString);
        Object result = ast.accept(evaluator);
        if (!returnString && result instanceof String) {
            throw new IllegalArgumentException("Expected to get a numeric result, but received a string of: " + result);
        }
        return result;
    }

    // Example usage
    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", 12L);
        params.put("key.device", 3L);

        // Example expressions
        String[] expressions = {
            "$key.device",
            "2 + 3 * 4",
            "$key * 2 + 5",
            "'Hello' & 'World-' & $key",
            "($key + 10) * 2",
            "'Value:' & ($key*2-3)",
            "NOW()",
            "DATE(NOW())",
            "DATE(NOW() - 90000000)",
            "DATE(NOW(), 'yyyy-MM-dd HH:mm:ss')",
            "DATE(NOW() -90000000, 'yyyy-MM-dd HH:mm:ss')",
            "PAD(42, 5, '0')",
            "PAD('ABC', 5, '*')",
            "PAD($key, 4, '0')",
            "PAD($key, 8, '-')",
            "PAD($key, 8, '-') +3",
            "UUID()",
            "UUID(1)",
            "UUID(1234567)",
            "UUID($key)"
        };

        ExpressionParser parser = new ExpressionParser();
        for (String expr : expressions) {
            try {
                System.out.println("Expression: " + expr);
                Object o = parser.evaluate(expr, params, true);
                System.out.println("String result: " + o + " of type " + o.getClass());
                o = parser.evaluate(expr, params, false);
                System.out.println("Numeric result: " + o + " of type " + o.getClass());
                System.out.println();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

