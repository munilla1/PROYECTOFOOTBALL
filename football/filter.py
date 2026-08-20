import subprocess  
targets = ["[ERROR] com.example.football.acceptance", "Status expected", "AssertionError"]  
p = subprocess.Popen('mvn test -Dtest=UsuarioAcceptanceTest,SesionAcceptanceTest', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='utf-8', errors='ignore')  
count = 0  
for line in p.stdout:  
    if any(t in line for t in targets):  
        print(line, end='')  
        count += 1  
        if count >= 50:  
            break  
p.wait() 
