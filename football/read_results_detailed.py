with open(log_path, encoding='utf-8', errors='ignore') as f: content = f.read()  
import re  
print("--- FAILURES AND ERRORS DETAILED ---")  
for block in content.split('[ERROR] Failures:'):  
    if len(block) > 1: print(block[:1500]) 
