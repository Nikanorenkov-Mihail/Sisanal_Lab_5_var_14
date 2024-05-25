import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static java.lang.Math.abs;

public class Algorithm {      // Класс различных функций
    //Parameters
    int N;                    // текущее число заявок в системе
    int N_post;               // число заявок, поступивших в систему с начала моделирования
    int N_k;                  // текущее число занятых каналов
    double[] T_osv; // ближайшие моменты времени освобождения приборов (массив, размерность K)
    double T;                    // текущее системное время
    double T_post;               // ближайший момент времени поступления заявки в систему
    double[] T_k;     // суммарные времена занятости каналов (массив, размерность K)
    double[] time_queue;       // хранение информации о времени поступления заявки
    //Enter parameters
    public int N_max;            // число реализаций – максимальное число заявок, поступивших в систему (вводимый параметр)
    public static int K;         // число каналов (вводимый параметр)
    public int IntensiveIn;      // интенсивность поступления заявок (вводимый параметр)
    public double IntensiveService; // интенсивность обслуживания (вводимый параметр)
    public int t_2;              // максимальное время нахождения заявки в очереди (параметр выхода заявки из очереди)

    //Generated_values
    public double t_osv;            // длительность обслуживания – длительность интервала между моментами начала обслуживания и освобождения прибора (случайная величина)
    public double t_post;           // длительность интервала между моментами поступления заявок (случайная величина)
    public double T_pred;           //момент времени предыдущего события (поступления, освобождения)

    // Results
    public double P_pr;             // вероятность простоя системы – вероятность отсутствия занятых каналов (результирующий показатель)
    public double K_z;              // коэффициент загрузки системы – среднее число занятых каналов (результирующий показатель)

    public void enter_parameters(String file_in) {
        //String[] lib = {"IntensiveIn", "IntensiveService", "N_max", "K"};
        String st;
        String[] words;
        try {
            File file = new File(file_in);
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((st = br.readLine()) != null) {
                words = st.replaceAll("\\s+", "").split(":");
                if (!words[1].equals("0"))
                    switch (words[0]) {
                        case ("IntensiveIn"):
                            IntensiveIn = Integer.parseInt(words[1]);
                            break;
                        case ("IntensiveService"):
                            IntensiveService = Double.parseDouble(words[1]);
                            break;
                        case ("N_max"):
                            N_max = Integer.parseInt(words[1]);
                            break;
                        case ("K"):
                            K = Integer.parseInt(words[1]);
                            break;
                        case ("t_2"):
                            t_2 = Integer.parseInt(words[1]);
                            break;
                        default:
                            System.out.println("Ошибка во входном файле");
                            break;
                    }
            }
        } catch (Exception e) {
            System.out.println("Exception on read file");
        }
    }


    public void defaultValues() {
        this.N = 0;      // текущее число заявок в системе
        this.N_post = 0; // число заявок, поступивших в систему с начала моделирования
        this.N_k = 0;    // текущее число занятых каналов
        this.T = 0;      // текущее системное время
        this.T_post = 0; // ближайший момент времени поступления заявки в систему
        T_k = new double[K];
        T_osv = new double[K];
        time_queue = new double[N_max];
        Arrays.fill(this.T_k, 0);    // суммарные времена занятости каналов (массив, размерность K)
        Arrays.fill(this.T_osv, 99999999);  // ближайшие моменты времени освобождения приборов (массив, размерность K)
        Arrays.fill(this.time_queue, 0);
    }

    public void timeCheck() {
        for (int i = 0; i < this.time_queue.length; i++) {
            if (abs(this.T - this.time_queue[i]) > this.t_2 & this.time_queue[i] != 0.0)
                deleteTime(i);
        }
    }

    public void deleteTime(int number) {
        if (number == this.time_queue.length - 1) this.time_queue[number] = 0;
        else {
            for (int i = number; i < this.time_queue.length - 1; i++) {
                this.time_queue[i] = this.time_queue[i + 1];
            }
            System.out.println("Заявка номер(-):" + (number + 1) + " удалена из очереди (превышено время ожидания в очереди) | Текущее время: " + this.T + " | Заявка №2 становится заявкой №1");
        }
    }

    public void addTime() {
        boolean flagToOver = false;
        for (int i = 0; i < this.time_queue.length - 1; i++) {
            if (this.time_queue[i] == 0.0 & !flagToOver) {
                this.time_queue[i] = this.T;
                flagToOver = true;
                System.out.println("Заявка номер(+): " + (i + 1) + " | Поступила в: " + this.T);
                i = this.time_queue.length - 1;
            }
        }
        if (!flagToOver) System.out.println("Слишком много заявок поступает в очередь");
    }

    public double Form_rand() {
        // Parameters for Random
        int min = 0;
        int max = 1;
        return min + Math.random() * (max - min);
    }

    public double Form_t_post() {
        double y = Form_rand();
        return (-1 * ((double) 1 / IntensiveIn) * Math.log(y));
    }

    public double Form_t_osv() {
        double y = Form_rand();
        return (-1 * (1 / IntensiveService) * Math.log(y));
    }

    public double Form_K_z() {
        double sum = 0;
        for (int i = 0; i < K; i++) {
            sum += (i + 1) * (this.T_k[i] / this.T);
        }
        return sum;
    }

    public double minOf_T_osv() {
        double min = 999999999.9;
        for (double j : this.T_osv) {
            if (j < min) min = j;
        }
        return min;
    }

    public boolean StoppingCondition() {
        return (this.N_post >= this.N_max);
    }
/*
    public void show() {
        System.out.println("T_pred " + this.T_pred);
        System.out.println("T " + this.T);
        System.out.println("T_post " + this.T_post);
        System.out.println("N_post " + this.N_post);
        System.out.println("T_osv " + minOf_T_osv());
        System.out.println("t_osv " + t_osv);
        System.out.println("t_post " + t_post);
        System.out.println("__________");
    }

 */

    public void showResults() {
        System.out.println("__________Results__________");
        System.out.println("Result Ppr: " + this.P_pr);
        System.out.println("Result Kz: " + this.K_z);
        System.out.println("__________Results__________");
    }


    public Algorithm(String inputFile) {
        //enter_parameters("input_var_14.txt");
        enter_parameters(inputFile);

        defaultValues();

        this.t_post = Form_t_post();

        this.T_post += this.t_post;

        while (!StoppingCondition()) {
            this.T_pred = this.T;

            if (this.t_post < minOf_T_osv()) {
                this.T = this.T_post;
                timeCheck();
                this.N++;
                addTime();
                //show();
                this.T_k[N_k] += this.T - this.T_pred;

                if (!(this.N >= K)) {
                    // i = j: Tосв[j] = T  Условие не нужно, так как T_osv позже целиком перезаполнится
                    this.t_osv = Form_t_osv();
                    Arrays.fill(this.T_osv, this.T + t_osv);
                    N_k++;
                }
                this.t_post = Form_t_post();

                T_post += t_post;
                N_post++;
                //show();

                if (StoppingCondition()) {
                    this.P_pr = this.T_k[0] / this.T;
                    this.K_z = Form_K_z();
                    showResults();
                }

            } else {
                this.T = minOf_T_osv();
                timeCheck();
                if (this.N > 0) {
                    this.N--;
                    this.T_k[this.N_k] += this.T - this.T_pred;

                    //Arrays.fill(this.T_osv, this.T);   Условие не нужно, так как T_osv позже целиком перезаполнится
                    if (this.N < K) {
                        Arrays.fill(this.T_osv, 99999999);
                        this.N_k--;
                    } else {
                        Arrays.fill(this.T_osv, this.T + this.t_osv);
                    }

                } else {
                    System.out.println("Отсутствуют заявки в системе");
                    System.out.println("Время: " + this.T);
                }
            }
        }
    }


}
