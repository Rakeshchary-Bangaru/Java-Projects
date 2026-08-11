import java.util.Scanner;

public class QuestionService {

    private Question[] questions = new Question[5];
    private String[] selections = new String[5];

    public QuestionService() {

        questions[0] = new Question(
                1,
                "What is the size of int?",
                new String[]{"2 bytes", "6 bytes", "4 bytes", "8 bytes"},
                "4 bytes"
        );

        questions[1] = new Question(
                2,
                "What is the size of double?",
                new String[]{"2 bytes", "6 bytes", "4 bytes", "8 bytes"},
                "8 bytes"
        );

        questions[2] = new Question(
                3,
                "Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"},
                "extends"
        );

        questions[3] = new Question(
                4,
                "Which method is the entry point of a Java program?",
                new String[]{"start()", "run()", "main()", "init()"},
                "main()"
        );

        questions[4] = new Question(
                5,
                "Which keyword refers to the current object?",
                new String[]{"this", "super", "static", "new"},
                "this"
        );
    }

    public void playQuiz() {

        Scanner input = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("        WELCOME TO JAVA QUIZ");
        System.out.println("==================================");
        System.out.println("Choose an option between 1 and 4.");

        for (int i = 0; i < questions.length; i++) {

            Question q = questions[i];

            System.out.println("\n----------------------------------");
            System.out.println("Question " + q.getId());
            System.out.println("----------------------------------");

            System.out.println(q.getQuestion());

            String[] options = q.getOptions();

            for (int j = 0; j < options.length; j++) {
                System.out.println((j + 1) + ". " + options[j]);
            }

            int choice;

            while (true) {

                System.out.print("Enter your option (1-4): ");

                // Check whether the user entered an integer
                if (input.hasNextInt()) {

                    choice = input.nextInt();

                    // Check whether the integer is between 1 and 4
                    if (choice >= 1 && choice <= 4) {
                        break;
                    }

                    System.out.println(
                            "Invalid option. Please enter a number between 1 and 4."
                    );

                } else {

                    System.out.println(
                            "Invalid input. Please enter a number between 1 and 4."
                    );

                    // Remove the invalid input from Scanner
                    input.next();
                }
            }

            // User enters 1-4, but array indexes are 0-3
            selections[i] = options[choice - 1];
        }

        input.close();
    }

    public void printScore() {

        int score = 0;

        System.out.println("\n\n==================================");
        System.out.println("           QUIZ RESULTS");
        System.out.println("==================================");

        for (int i = 0; i < questions.length; i++) {

            Question q = questions[i];

            System.out.println("\nQuestion " + q.getId() + ": "
                    + q.getQuestion());

            System.out.println("Your Answer   : " + selections[i]);
            System.out.println("Correct Answer: " + q.getAnswer());

            if (selections[i].equals(q.getAnswer())) {

                System.out.println("Result        : Correct");
                score++;

            } else {

                System.out.println("Result        : Incorrect");
            }
        }

        System.out.println("\n==================================");
        System.out.println("Final Score: " + score + " / " + questions.length);
        System.out.println("==================================");
    }
}