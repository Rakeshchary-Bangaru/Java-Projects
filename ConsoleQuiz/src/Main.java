public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Java Quiz!");
        QuestionService service = new QuestionService();
        service.playQuiz();
        service.printScore();
    }
}