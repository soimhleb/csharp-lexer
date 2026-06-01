import java.util.*;
import java.io.*;

public class Lexer {
    private String filename;
    private String content;
    private int pos;
    private int line;
    private int column;
    private char currentChar;
    private Statistics stats;

    private TokenType getKeywordType(String word) {
        switch (word) {
            case "abstract": return TokenType.ABSTRACT;
            case "as": return TokenType.AS;
            case "base": return TokenType.BASE;
            case "bool": return TokenType.BOOL;
            case "break": return TokenType.BREAK;
            case "byte": return TokenType.BYTE;
            case "case": return TokenType.CASE;
            case "catch": return TokenType.CATCH;
            case "char": return TokenType.CHAR_KEYWORD;
            case "checked": return TokenType.CHECKED;
            case "class": return TokenType.CLASS;
            case "const": return TokenType.CONST;
            case "continue": return TokenType.CONTINUE;
            case "decimal": return TokenType.DECIMAL;
            case "default": return TokenType.DEFAULT;
            case "delegate": return TokenType.DELEGATE;
            case "do": return TokenType.DO;
            case "double": return TokenType.DOUBLE;
            case "else": return TokenType.ELSE;
            case "enum": return TokenType.ENUM;
            case "event": return TokenType.EVENT;
            case "explicit": return TokenType.EXPLICIT;
            case "extern": return TokenType.EXTERN;
            case "false": return TokenType.FALSE;
            case "finally": return TokenType.FINALLY;
            case "fixed": return TokenType.FIXED;
            case "float": return TokenType.FLOAT_KEYWORD;
            case "for": return TokenType.FOR;
            case "foreach": return TokenType.FOREACH;
            case "goto": return TokenType.GOTO;
            case "if": return TokenType.IF;
            case "implicit": return TokenType.IMPLICIT;
            case "in": return TokenType.IN;
            case "int": return TokenType.INT;
            case "interface": return TokenType.INTERFACE;
            case "internal": return TokenType.INTERNAL;
            case "is": return TokenType.IS;
            case "lock": return TokenType.LOCK;
            case "long": return TokenType.LONG;
            case "namespace": return TokenType.NAMESPACE;
            case "new": return TokenType.NEW;
            case "null": return TokenType.NULL;
            case "object": return TokenType.OBJECT;
            case "operator": return TokenType.OPERATOR_KEYWORD;
            case "out": return TokenType.OUT;
            case "override": return TokenType.OVERRIDE;
            case "params": return TokenType.PARAMS;
            case "private": return TokenType.PRIVATE;
            case "protected": return TokenType.PROTECTED;
            case "public": return TokenType.PUBLIC;
            case "readonly": return TokenType.READONLY;
            case "ref": return TokenType.REF;
            case "return": return TokenType.RETURN;
            case "sbyte": return TokenType.SBYTE;
            case "sealed": return TokenType.SEALED;
            case "short": return TokenType.SHORT;
            case "sizeof": return TokenType.SIZEOF;
            case "stackalloc": return TokenType.STACKALLOC;
            case "static": return TokenType.STATIC;
            case "string": return TokenType.STRING;
            case "struct": return TokenType.STRUCT;
            case "switch": return TokenType.SWITCH;
            case "this": return TokenType.THIS;
            case "throw": return TokenType.THROW;
            case "true": return TokenType.TRUE;
            case "try": return TokenType.TRY;
            case "typeof": return TokenType.TYPEOF;
            case "uint": return TokenType.UINT;
            case "ulong": return TokenType.ULONG;
            case "unchecked": return TokenType.UNCHECKED;
            case "unsafe": return TokenType.UNSAFE;
            case "ushort": return TokenType.USHORT;
            case "using": return TokenType.USING;
            case "virtual": return TokenType.VIRTUAL;
            case "void": return TokenType.VOID;
            case "volatile": return TokenType.VOLATILE;
            case "while": return TokenType.WHILE;
            default: return null;
        }
    }

    // Операторы (максимальной длины)
    private static final List<String> OPERATORS = Arrays.asList(
            "++", "--", "==", "!=", "<=", ">=", "&&", "||", "<<", ">>", "+=", "-=", "*=", "/=", "%=",
            "&=", "|=", "^=", "<<=", ">>=", "->", "=>", "::", "?.", "?[", "??", "?:", "+", "-", "*",
            "/", "%", "=", "!", "<", ">", "&", "|", "^", "~", "?"
    );

    public Lexer(String filename, String content, Statistics stats) {
        this.filename = filename;
        this.content = content;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.stats = stats;
        if (content.length() > 0) {
            currentChar = content.charAt(0);
        } else {
            currentChar = '\0';
        }
    }

    private void error(String message, int line, int column) {
        System.err.printf("Lexical error in file '%s', line %d, column %d: %s%n",
                filename, line, column, message);
        // Можно продолжать, но текущий токен пропустить
    }

    private void advance() {
        if (pos < content.length() - 1) {
            pos++;
            currentChar = content.charAt(pos);
            column++;
            if (currentChar == '\n') {
                line++;
                column = 1;
            }
        } else {
            currentChar = '\0'; // конец строки
        }
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isBinDigit(char c) {
        return c == '0' || c == '1';
    }

    public void tokenize() {
        while (currentChar != '\0') {
            skipWhitespace();
            if (currentChar == '\0') break;

            int startLine = line;
            int startCol = column;

            // Определяем тип лексемы по первому символу
            if (isLetter(currentChar)) {
                // идентификатор или ключевое слово
                StringBuilder sb = new StringBuilder();
                while (isLetter(currentChar) || isDigit(currentChar)) {
                    sb.append(currentChar);
                    advance();
                }
                String word = sb.toString();
                TokenType type = getKeywordType(word);
                if (type == null) {
                    type = TokenType.IDENTIFIER;
                }
                Token token = new Token(type, word, startLine, startCol);
                stats.addToken(token, filename);
            }
            else if (isDigit(currentChar)) {
                StringBuilder sb = new StringBuilder();
                // Проверка на шестнадцатеричный литерал
                if (currentChar == '0' && (peek() == 'x' || peek() == 'X')) {
                    sb.append(currentChar);
                    advance(); // переходим на 'x'/'X'
                    sb.append(currentChar);
                    advance();
                    // Читаем шестнадцатеричные цифры
                    while (isHexDigit(currentChar)) {
                        sb.append(currentChar);
                        advance();
                    }
                    // Суффиксы U, L, UL и т.д. можно добавить по желанию
                    Token token = new Token(TokenType.INTEGER_LITERAL, sb.toString(), startLine, startCol);
                    stats.addToken(token, filename);
                } else if (currentChar == '0' && (peek() == 'b' || peek() == 'B')) {
                    sb.append(currentChar);
                    advance(); // переходим на 'b'/'B'
                    sb.append(currentChar);
                    advance();
                    // Читаем двоичные цифры (0 или 1)
                    while (isBinDigit(currentChar)) {
                        sb.append(currentChar);
                        advance();
                    }
                    // (Опционально) Добавить поддержку суффиксов (U, L, UL)
                    Token token = new Token(TokenType.INTEGER_LITERAL, sb.toString(), startLine, startCol);
                    stats.addToken(token, filename);
                } else {
                    // Обычное десятичное число
                    while (isDigit(currentChar)) {
                        sb.append(currentChar);
                        advance();
                    }
                    // Проверка на вещественный с точкой и экспонентой
                    if (currentChar == '.') {
                        sb.append('.');
                        advance();
                        while (isDigit(currentChar)) {
                            sb.append(currentChar);
                            advance();
                        }
                        if (currentChar == 'e' || currentChar == 'E') {
                            sb.append(currentChar);
                            advance();
                            if (currentChar == '+' || currentChar == '-') {
                                sb.append(currentChar);
                                advance();
                            }
                            while (isDigit(currentChar)) {
                                sb.append(currentChar);
                                advance();
                            }
                        }
                        if (currentChar == 'F' || currentChar == 'f' ||
                                currentChar == 'D' || currentChar == 'd' ||
                                currentChar == 'M' || currentChar == 'm') {
                            sb.append(currentChar);
                            advance();
                        }
                        Token token = new Token(TokenType.REAL_LITERAL, sb.toString(), startLine, startCol);
                        stats.addToken(token, filename);
                    } else {
                        // Целое десятичное
                        // Суффиксы L, U, UL...
                        if (currentChar == 'L' || currentChar == 'l' ||
                                currentChar == 'U' || currentChar == 'u') {
                            sb.append(currentChar);
                            advance();
                            // Поддержка 'ul' и 'lu' (упрощённо)
                            if ((currentChar == 'U' || currentChar == 'u') && (sb.charAt(sb.length()-1) == 'L' || sb.charAt(sb.length()-1) == 'l')) {
                                sb.append(currentChar);
                                advance();
                            }
                        }
                        Token token = new Token(TokenType.INTEGER_LITERAL, sb.toString(), startLine, startCol);
                        stats.addToken(token, filename);
                    }
                }
            }
            else if (currentChar == '"') {
                // строковый литерал (упрощённо, без поддержки verbatim и интерполяции)
                StringBuilder sb = new StringBuilder();
                sb.append(currentChar);
                advance();
                while (currentChar != '"' && currentChar != '\0' && currentChar != '\n') {
                    if (currentChar == '\\') {
                        sb.append(currentChar);
                        advance();
                        if (currentChar == '\0') break;
                        sb.append(currentChar);
                        advance();
                    } else {
                        sb.append(currentChar);
                        advance();
                    }
                }
                if (currentChar == '"') {
                    sb.append(currentChar);
                    advance();
                    Token token = new Token(TokenType.STRING_LITERAL, sb.toString(), startLine, startCol);
                    stats.addToken(token, filename);
                } else {
                    error("Unclosed string literal", startLine, startCol);
                }
            }
            else if (currentChar == '\'') {
                // символьный литерал
                StringBuilder sb = new StringBuilder();
                sb.append(currentChar);
                advance();
                // хотя бы один символ (может быть escape)
                if (currentChar == '\\') {
                    sb.append(currentChar);
                    advance();
                    if (currentChar != '\0') {
                        sb.append(currentChar);
                        advance();
                    } else {
                        error("Unclosed character literal", startLine, startCol);
                    }
                } else if (currentChar != '\'' && currentChar != '\0') {
                    sb.append(currentChar);
                    advance();
                } else {
                    error("Empty character literal", startLine, startCol);
                }
                if (currentChar == '\'') {
                    sb.append(currentChar);
                    advance();
                    Token token = new Token(TokenType.CHAR_LITERAL, sb.toString(), startLine, startCol);
                    stats.addToken(token, filename);
                } else {
                    error("Unclosed character literal", startLine, startCol);
                }
            }
            else if (currentChar == '/' && peek() == '/') {
                // однострочный комментарий – игнорируем
                while (currentChar != '\n' && currentChar != '\0') advance();
            }
            else if (currentChar == '/' && peek() == '*') {
                // многострочный комментарий – игнорируем
                advance(); advance();
                while (!(currentChar == '*' && peek() == '/') && currentChar != '\0') {
                    advance();
                }
                if (currentChar == '*') {
                    advance(); advance(); // закрываем */
                } else {
                    error("Unclosed multi-line comment", startLine, startCol);
                }
            }
            else if (currentChar == '#') {
                // директива препроцессора – собираем до конца строки
                StringBuilder sb = new StringBuilder();
                sb.append(currentChar);
                advance();
                while (currentChar != '\n' && currentChar != '\0') {
                    sb.append(currentChar);
                    advance();
                }
                Token token = new Token(TokenType.PREPROCESSOR, sb.toString(), startLine, startCol);
                stats.addToken(token, filename);
            }
            else {
                // операторы и разделители
                String op = readOperator();
                if (op != null) {
                    Token token = new Token(TokenType.OPERATOR, op, startLine, startCol);
                    stats.addToken(token, filename);
                } else if (isSeparator(currentChar)) {
                    Token token = new Token(TokenType.SEPARATOR, String.valueOf(currentChar), startLine, startCol);
                    stats.addToken(token, filename);
                    advance();
                } else {
                    error("Invalid character '" + currentChar + "'", startLine, startCol);
                    advance(); // пропускаем
                }
            }
        }
        // Добавляем маркер конца файла (опционально)
        stats.addToken(new Token(TokenType.EOF, "", line, column), filename);
    }

    private char peek() {
        if (pos + 1 < content.length()) return content.charAt(pos + 1);
        return '\0';
    }

    private String readOperator() {
        // проверяем двух-трёхсимвольные операторы
        for (String op : OPERATORS) {
            if (content.startsWith(op, pos)) {
                pos += op.length();
                column += op.length();
                if (pos < content.length()) currentChar = content.charAt(pos);
                else currentChar = '\0';
                return op;
            }
        }
        return null;
    }

    private boolean isSeparator(char c) {
        return ";,.:()[]{}".indexOf(c) != -1;
    }

    private void skipWhitespace() {
        while (currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r') {
            if (currentChar == '\n') {
                line++;
                column = 1;
            }
            advance();
        }
    }
}