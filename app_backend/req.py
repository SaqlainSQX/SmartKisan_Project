import pkg_resources

def generate_requirements_pure_python(output_file="requirements.txt"):
    with open(output_file, "w") as f:
        for dist in pkg_resources.working_set:
            f.write(f"{dist.project_name}=={dist.version}\n")
    print(f"✅ requirements.txt created with {len(list(pkg_resources.working_set))} packages")

if __name__ == "__main__":
    generate_requirements_pure_python()
