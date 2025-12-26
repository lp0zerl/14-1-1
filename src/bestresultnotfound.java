class BestResultNotFound extends Exception {
    public BestResultNotFound(String searchTerm) {
        super("Не найден подходящий результат для запроса: \"" + searchTerm + "\"");
    }
}
