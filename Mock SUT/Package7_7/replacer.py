import os
import shutil

current_directory = os.getcwd()

def replace_and_execute_scripts(root_dir, source_file, target_file):
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file == source_file:
                source_path = os.path.join('./BaseFiles', target_file)
                target_path = os.path.join(root, target_file)
                shutil.copy(source_path, target_path)
                os.chdir(root)
                os.system(f"python3 {target_file}")
                os.chdir(current_directory)
                print(f"Finished with {root}")

# Example usage
root_directory = "./2023_08_08_16_16_52/"
source_file_name = "visualizer.py"
target_file_name = "visualizer.py"

replace_and_execute_scripts(root_directory, source_file_name, target_file_name)
