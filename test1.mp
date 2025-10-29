programa_minipar

SEQ 
  print("Iniciando Fatorial(5) e Fibonacci(8) em PARALELO...")
  n_fat = 5
  n_fib = 8

PAR 

  
  SEQ 
    
    print("--- [Thread Fatorial] Iniciando ---")
    print("Calculando fatorial de:")
    print(n_fat)
    
    fat = 1
    i = 1
    
    while i <= n_fat { 
        fat = fat * i
        i = i + 1
    } 
    
    print("--- [Thread Fatorial] Resultado do fatorial:")
    print(fat)
    print("--- [Thread Fatorial] Finalizado ---")
  

  
  SEQ 
    
    print("--- [Thread Fibonacci] Iniciando ---")
    print("Série de Fibonacci até o termo:")
    print(n_fib)
    
    a = 0
    b = 1
    count = 0
    
    while count < n_fib { 
        print(a) 
        temp = a + b
        a = b
        b = temp
        count = count + 1
    } 
    print("--- [Thread Fibonacci] Finalizado ---")
  

