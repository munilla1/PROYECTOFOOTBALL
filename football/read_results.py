import sys  
log_path = r'c:\Users\mucho\AppData\Roaming\Code\User\workspaceStorage\26eea397e859f2200dae108417d15f8e\GitHub.copilot-chat\chat-session-resources\40c46002-5188-4aff-aba1-d6fd2fefa786\call_MHwzdDhJNzRwaEhHeFo1UXFkNEc__vscode-1787239057716\content.txt'  
with open(log_path, encoding='utf-8', errors='ignore') as f: lines = f.readlines()  
print("--- SUMARIO Y ERRORES ---")  
for line in lines:  
    if "Tests run:" in line or "Failure:" in line or "Error:" in line or "Errors:" in line or "Failures:" in line or "<<< FAILURE!" in line or "<<< ERROR!" in line: print(line.strip()) 
