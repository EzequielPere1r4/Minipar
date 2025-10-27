programa_minipar

SEQ
  print("Digite um número para o fatorial:")
  n = input()
  
  fatorial = 1

  while (n > 0) {
    fatorial = fatorial * n
    n = n - 1
  }

  print("O fatorial é:")
  print(fatorial)
